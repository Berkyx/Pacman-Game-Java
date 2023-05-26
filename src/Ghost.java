import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.List;
public class Ghost {
    private int ghostX;
    private int ghostY;
    private int initialX;
    private int initialY;
    private int prevGhostX;
    private int prevGhostY;
    private int moveDelay;
    private int moveCounter;
    private Direction direction;
    private Image ghostImage;
    private List<Point> path;
    private PacmanModel model;
    private boolean isUpdated = false;
    private volatile boolean isWeakened = false;
    private volatile long lastSpecialFoodTime = 0;

    public Ghost(int x, int y, String imagePath, PacmanModel model, int moveDelay) {
        this.model = model;
        this.ghostX = x;
        this.ghostY = y;
        this.initialX = x;
        this.initialY = y;
        this.prevGhostX = x;
        this.prevGhostY = y;
        this.direction = Direction.NONE;
        this.moveDelay = moveDelay;
        this.moveCounter = 0;
        try {
            ghostImage = ImageIO.read(new File(imagePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
        path = new ArrayList<>();
        new Thread(() -> {
            while (true) {
                try {
                    updatePathToPacman();
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.out.println("Thread was interrupted, Failed to complete operation");
                }
            }
        }).start();
    }
    public synchronized void updatePathToPacman() {
        int pacmanX = model.getPacmanX();
        int pacmanY = model.getPacmanY();
        boolean[][] maze = model.getMaze();
        path = AStar.getPath(maze, new Point(ghostX, ghostY), new Point(pacmanX, pacmanY));
    }
    public int getGhostX() {
        return ghostX;
    }
    public int getPrevGhostX() {
        return prevGhostX;
    }
    public int getPrevGhostY() {
        return prevGhostY;
    }
    public int getGhostY() {
        return ghostY;
    }
    public void respawn() {
        this.ghostX = this.initialX;
        this.ghostY = this.initialY;
    }
    public void moveGhost(boolean[][] maze, PacmanModel model) {
        if (isWeakened()) {
            moveCounter++;
            if (moveCounter < moveDelay * 2) {
                return;
            }
        } else {
            moveCounter++;
            if (moveCounter < moveDelay) {
                return;
            }
        }
        moveCounter = 0;
        prevGhostX = ghostX;
        prevGhostY = ghostY;
        Point pacmanPosition = model.getPacmanPosition();
        if (Math.abs(pacmanPosition.x - ghostX) <= 5 && Math.abs(pacmanPosition.y - ghostY) <= 5) {
            // If Pacman is within 5x5 cells, calculate the shortest path to Pacman
            updatePathToPacman();
        } else if (path.isEmpty()) {
            // If the ghost is not pursuing Pacman and doesn't have a path, choose a random move
            List<Point> possibleMoves = getPossibleMoves(maze);
            Point randomMove = possibleMoves.get(new Random().nextInt(possibleMoves.size()));
            path.add(randomMove);
        }
        if (path.size() > 1) {
            Point next = path.get(1);
            if (model.getCellSemaphore(next.x, next.y).tryAcquire()) {
                model.getCellSemaphore(ghostX, ghostY).release();
                ghostX = next.x;
                ghostY = next.y;
                path.remove(1);
            }
        } else {
            if (direction == Direction.NONE || hitsWall(maze)) {
                direction = Direction.values()[new Random().nextInt(Direction.values().length)];
            }
            switch (direction) {
                case UP:
                    if (ghostY - 1 >= 0 && !maze[ghostY - 1][ghostX] && model.getCellSemaphore(ghostX, ghostY - 1).tryAcquire()) {
                        model.getCellSemaphore(ghostX, ghostY).release();
                        ghostY--;
                    }
                    break;
                case DOWN:
                    if (ghostY + 1 < maze.length && !maze[ghostY + 1][ghostX] && model.getCellSemaphore(ghostX, ghostY + 1).tryAcquire()) {
                        model.getCellSemaphore(ghostX, ghostY).release();
                        ghostY++;
                    }
                    break;
                case LEFT:
                    if (ghostX - 1 >= 0 && !maze[ghostY][ghostX - 1] && model.getCellSemaphore(ghostX - 1, ghostY).tryAcquire()) {
                        model.getCellSemaphore(ghostX, ghostY).release();
                        ghostX--;
                    }
                    break;
                case RIGHT:
                    if (ghostX + 1 < maze[0].length && !maze[ghostY][ghostX + 1] && model.getCellSemaphore(ghostX + 1, ghostY).tryAcquire()) {
                        model.getCellSemaphore(ghostX, ghostY).release();
                        ghostX++;
                    }
                    break;
            }
        }
        if (new Random().nextInt(4) == 0) { // 25% chance
            model.generateSpecialFood(prevGhostX, prevGhostY);
        }
        isUpdated = true;
    }
    private List<Point> getPossibleMoves(boolean[][] maze) {
        List<Point> moves = new ArrayList<>();
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        for (int[] direction : directions) {
            int newX = this.getGhostX() + direction[0];
            int newY = this.getGhostY() + direction[1];
            if (newX >= 0 && newX < maze[0].length && newY >= 0 && newY < maze.length && !maze[newY][newX]) {
                moves.add(new Point(newX, newY));
            }
        }
        return moves;
    }
    public void resetUpdateFlag() {
        isUpdated = false;
    }
    public void weaken() {
        isWeakened = true;
        lastSpecialFoodTime = System.currentTimeMillis();
    }
    public void unweaken() {
        if (System.currentTimeMillis() - lastSpecialFoodTime >= 30000) {
            isWeakened = false;
        }
    }
    public boolean isWeakened() {
        return isWeakened;
    }
    private boolean hitsWall(boolean[][] maze) {
        switch (direction) {
            case UP:
                return ghostY - 1 < 0 || maze[ghostY - 1][ghostX];
            case DOWN:
                return ghostY + 1 >= maze.length || maze[ghostY + 1][ghostX];
            case LEFT:
                return ghostX - 1 < 0 || maze[ghostY][ghostX - 1];
            case RIGHT:
                return ghostX + 1 >= maze[0].length || maze[ghostY][ghostX + 1];
        }
        return false;
    }
}
