import javax.swing.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PacmanController {
    private PacmanModel model;
    private PacmanView view;
    private MainMenu mainMenu;
    private int height;
    private int width;
    private int lastKeyPressed = KeyEvent.VK_RIGHT;
    private final Object ghostLock = new Object();
    private List<Thread> ghostThreads = Collections.synchronizedList(new ArrayList<>());

    public PacmanController(PacmanModel model, PacmanView view, int height, int width, MainMenu mainMenu) {
        this.model = model;
        this.view = view;
        this.height = height;
        this.width = width;
        this.mainMenu = mainMenu;
        shortcut(this.view.frame);
        view.drawMaze(model);
        this.view.frame.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int oldX = model.getPacmanX();
                int oldY = model.getPacmanY();
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_UP:
                        model.movePacman(0, -1, width, height);
                        break;
                    case KeyEvent.VK_DOWN:
                        model.movePacman(0, 1, width, height);
                        break;
                    case KeyEvent.VK_LEFT:
                        model.movePacman(-1, 0, width, height);
                        break;
                    case KeyEvent.VK_RIGHT:
                        model.movePacman(1, 0, width, height);
                        break;
                }
                lastKeyPressed = e.getKeyCode();
                view.updatePacmanPosition(oldX, oldY, oldX, oldY, -1, lastKeyPressed);
                view.updateScore(model.getScore());
            }
        });
        this.view.frame.setFocusable(true);
        this.view.frame.requestFocusInWindow();
        // Ghost spawn thread
        new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    synchronized (ghostLock) {
                        if (ghostThreads.size() < 10) {
                            model.spawnGhost("resources/images/ghost/white/1.png");
                            Ghost ghost = model.getGhosts().get(model.getGhosts().size() - 1);
                            Thread ghostThread = new Thread(new GhostMover(ghost));
                            ghostThread.start();
                            ghostThreads.add(ghostThread);
                        }
                    }
                    Thread.sleep(60000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.out.println("Spawning thread was interrupted, Failed to complete operation");
                }
            }
        }).start();
        // Game update thread
        new Thread(() -> {
            while (true) {
                try {
                    model.setCurrentFrame((model.getCurrentFrame() + 1) % 5);
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.out.println("Game update thread was interrupted, Failed to complete operation");
                }
            }
        }).start();
        // Rendering thread
        new Thread(() -> {
            while (true) {
                try {
                    SwingUtilities.invokeLater(() -> {
                        view.clearBuffer();
                        view.drawMaze(model);
                        view.updatePacmanPosition(model.getPacmanX(), model.getPacmanY(), model.getPacmanX(), model.getPacmanY(), model.getCurrentFrame(), lastKeyPressed);
                        for (int i = 0; i < model.getMaze().length; i++) {
                            for (int j = 0; j < model.getMaze()[0].length; j++) {
                                if (model.getFood()[i][j] != 0) {
                                    view.updateFoodPosition(j, i, model.getFood()[i][j]);
                                }
                            }
                        }
                        List<Ghost> ghosts = model.getGhosts();
                        for (Ghost ghost : ghosts) {
                            view.updateGhostPosition(ghost);
                            ghost.resetUpdateFlag();
                        }
                        view.updateLife(model.getLives());
                        view.updateTime(model.getGameTime());
                        view.repaint();
                    });
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.out.println("Rendering thread was interrupted, Failed to complete operation");
                }
            }
        }).start();
    }
    class GhostMover implements Runnable {
        private final Ghost ghost;
        GhostMover(Ghost ghost) {
            this.ghost = ghost;
        }
        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    synchronized (ghostLock) {
                        ghost.moveGhost(model.getMaze(), model);
                        if (ghost.getGhostX() == model.getPacmanX() && ghost.getGhostY() == model.getPacmanY()) {
                            if (ghost.isWeakened()) {
                                ghost.respawn();
                                model.setScore(model.getScore() + 10);
                            } else {
                                model.setLives(model.getLives() - 1);
                                model.respawnPacman();
                                if (model.getLives() == 0) {
                                    model.setGameOver(true);
                                    break;
                                }
                            }
                        }
                    }
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.out.println("Ghost thread was interrupted, Failed to complete operation");
                }
            }
        }
    }
    public void interruptThreads() {
        for (Thread thread : ghostThreads) {
            thread.interrupt();
        }
    }
    private void shortcut(JFrame frame) {
        frame.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK), "quit");
        frame.getRootPane().getActionMap().put("quit", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                interruptThreads();
                SwingUtilities.invokeLater(() -> {
                    frame.dispose();
                    mainMenu.showMenu();
                });
            }
        });
    }
}

