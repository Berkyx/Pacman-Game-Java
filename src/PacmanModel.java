import javax.swing.*;
import java.awt.Point;
import java.util.Random;
import java.util.Stack;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;



public class PacmanModel {
    private int pacmanX;
    private int pacmanY;
    private int initialPacmanX;
    private int initialPacmanY;
    private int currentFrame = 0;
    private boolean[][] maze;
    private Random random = new Random();
    private List<Ghost> ghosts;
    private Semaphore[][] cellSemaphores;
    private int[][] food;
    private int score = 0;
    private int lives = 3;
    private int totalFoodCount = 0;
    private boolean gameIsOver = false;
    private Thread gameTimerThread;
    private volatile boolean running;
    private int gameTime = 0;
    public PacmanModel(int width, int height) {
        maze = new boolean[height][width];
        food = new int[height][width];
        generateMaze();
        int pacmanSpawnSize = 5;
        this.pacmanX = pacmanSpawnSize / 2;
        this.pacmanY = height - 1 - pacmanSpawnSize / 2;
        this.initialPacmanX = pacmanSpawnSize / 2;
        this.initialPacmanY = height - 1 - pacmanSpawnSize / 2;
        this.ghosts = new ArrayList<>();
        cellSemaphores = new Semaphore[height][width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                cellSemaphores[i][j] = new Semaphore(1, true);
            }
        }
        this.running = true;
        this.gameTimerThread = new Thread(() -> {
            while (running) {
                try {
                    Thread.sleep(1000);
                    gameTime++;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        this.gameTimerThread.start();
    }
    public void setScore(int score) {
        this.score = score;
    }
    public void setLives(int lives) {
        this.lives = lives;
    }
    public int getLives() {
        return lives;
    }
    public void setGameOver(boolean gameIsOver) {
        this.gameIsOver = gameIsOver;
        if (gameIsOver) {
            this.running = false;
            EndGameScreen endGameScreen = new EndGameScreen(score, gameTime);
            endGameScreen.setVisible(true);
            endGameScreen.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    super.windowClosed(e);
                    System.exit(0);
                }
            });

        }
    }
    public int getScore() {
        return score;
    }
    public int[][] getFood() {
        return food;
    }
    public List<Ghost> getGhosts() {
        return ghosts;
    }
    public int getGameTime() {
        return gameTime;
    }
    public void respawnPacman() {
        this.pacmanX = this.initialPacmanX;
        this.pacmanY = this.initialPacmanY;
    }
    public void spawnGhost(String ghostImage) {
        ghosts.add(new Ghost(maze[0].length - 1, 0, ghostImage, this,10));
    }
    public void setCurrentFrame(int currentFrame) {
        this.currentFrame = currentFrame;
    }
    public Semaphore getCellSemaphore(int x, int y) {
        return cellSemaphores[y][x];
    }
    public int getCurrentFrame() {
        return currentFrame;
    }
    public int getPacmanX() {
        return pacmanX;
    }
    public int getPacmanY() {
        return pacmanY;
    }
    public Point getPacmanPosition() {
        return new Point(pacmanX, pacmanY);
    }
    public boolean[][] getMaze() {
        return maze;
    }
    public void movePacman(int dx, int dy, int maxWidth, int maxHeight) {
        int newX = Math.max(0, Math.min(maxWidth - 1, pacmanX + dx));
        int newY = Math.max(0, Math.min(maxHeight - 1, pacmanY + dy));
        if (!maze[newY][newX]) {
            pacmanX = newX;
            pacmanY = newY;
        }
        if (food[newY][newX] == 1) {
            food[newY][newX] = 0;
            score++;
            totalFoodCount--;
            if (totalFoodCount == 0) {
                setGameOver(true);
            }
        }
        if (food[newY][newX] == 2) {
            food[newY][newX] = 0;
            for (Ghost ghost : ghosts) {
                ghost.weaken();
                new Thread(() -> {
                    try {
                        Thread.sleep(30000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    ghost.unweaken();
                }).start();
            }
        }
        for (Ghost ghost : ghosts) {
            if (ghost.getGhostX() == pacmanX && ghost.getGhostY() == pacmanY) {
                if (ghost.isWeakened()) {
                    ghost.respawn();
                    score += 10;
                } else {
                    lives--;
                    respawnPacman();
                    if (lives == 0) {
                        setGameOver(true);
                        break;
                    }
                }
            }
        }
    }
    public void generateSpecialFood(int x, int y) {
        food[y][x] = 0;
        totalFoodCount--;
        food[y][x] = 2;
    }
    public void generateMaze() {
        for (int i = 0; i < maze.length; i++) {
            for (int j = 0; j < maze[0].length; j++) {
                maze[i][j] = true;
            }
        }
        Stack<Point> stack = new Stack<>();
        Set<Point> visited = new HashSet<>();
        int startX = random.nextInt(maze[0].length);
        int startY = random.nextInt(maze.length);
        Point start = new Point(startX, startY);
        maze[startY][startX] = false;
        stack.push(start);
        visited.add(start);
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        while (!stack.isEmpty()) {
            Point cell = stack.peek();
            List<Point> unvisitedNeighbours = new ArrayList<>();
            for (int[] direction : directions) {
                int newX = cell.x + 2 * direction[0];
                int newY = cell.y + 2 * direction[1];
                Point neighbour = new Point(newX, newY);
                if (newX >= 0 && newX < maze[0].length && newY >= 0 && newY < maze.length && !visited.contains(neighbour)) {
                    unvisitedNeighbours.add(neighbour);
                }
            }
            if (unvisitedNeighbours.isEmpty()) {
                stack.pop();
            } else {
                Point chosenNeighbour = unvisitedNeighbours.get(random.nextInt(unvisitedNeighbours.size()));
                maze[chosenNeighbour.y][chosenNeighbour.x] = false;
                maze[cell.y + (chosenNeighbour.y - cell.y) / 2][cell.x + (chosenNeighbour.x - cell.x) / 2] = false;
                stack.push(chosenNeighbour);
                visited.add(chosenNeighbour);
            }
        }
        int numDoors = (maze.length * maze[0].length) / 5;  // Adjust this to change the number of 'doors'
        for (int i = 0; i < numDoors; i++) {
            int x, y;
            do {
                x = random.nextInt(maze[0].length);
                y = random.nextInt(maze.length);
            } while (!maze[y][x]);  // Keep generating random coordinates until we hit a wall
            maze[y][x] = false;  // Break a 'door' in the wall
        }
        int ghostSpawnSize = 5; //ghost spawn area
        for (int i = 0; i < ghostSpawnSize; i++) {
            for (int j = maze[0].length - ghostSpawnSize; j < maze[0].length; j++) {
                maze[i][j] = false;
            }
        }
        int pacmanSpawnSize = 5; //pacman spawn area
        for (int i = maze.length - pacmanSpawnSize; i < maze.length; i++) {
            for (int j = 0; j < pacmanSpawnSize; j++) {
                maze[i][j] = false;
            }
        }
        for (int i = 0; i < maze.length; i++) {
            for (int j = 0; j < maze[0].length; j++) {
                if (!maze[i][j]) {  // if the cell is open check if the cell is not in the ghost or pacman spawn area
                    if (!((i < ghostSpawnSize && j >= maze[0].length - ghostSpawnSize) // ghost spawn area
                            || (i >= maze.length - pacmanSpawnSize && j < pacmanSpawnSize))) { // pacman spawn area
                        food[i][j] = 1;
                        totalFoodCount++;// place food in the cell
                    }
                }
            }
        }
    }
}
