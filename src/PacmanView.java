import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.awt.image.BufferedImage;
import java.util.Map;

public class PacmanView {
    JFrame frame;
    JPanel topPanel;
    JLabel scoreLabel;
    JLabel lifeLabel;
    JLabel timeLabel;
    JLabel[][] labels;
    private BufferedImage buffer;
    private Graphics2D bufferGraphics;
    private ImageIcon[] pacmanIcons;
    private ImageIcon[][] rotatedPacmanIcons;
    private ImageIcon foodIcon;
    private ImageIcon specialFoodIcon;
    private boolean isFrameSizeSet = false;
    private Map<String, ImageIcon> iconCache = new HashMap<>();
    private int cellWidth;
    private int cellHeight;
    public PacmanView(int width, int height) {
        frame = new JFrame("Pacman Game");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        topPanel = new JPanel();
        topPanel.setBackground(Color.YELLOW);
        scoreLabel = new JLabel("Score: 0");
        lifeLabel = new JLabel("Life: 3");
        timeLabel = new JLabel("Time: 0");
        topPanel.add(scoreLabel);
        topPanel.add(lifeLabel);
        topPanel.add(timeLabel);
        frame.add(topPanel, BorderLayout.NORTH);
        JPanel gamePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (buffer != null) {
                    g.drawImage(buffer, 0, 0, this);
                }
            }
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(800, 800 - topPanel.getPreferredSize().height);
            }
        };
        gamePanel.setBackground(Color.BLACK);
        frame.add(gamePanel, BorderLayout.CENTER);
        frame.pack();
        cellWidth = gamePanel.getWidth() / width;
        cellHeight = gamePanel.getHeight() / height;
        buffer = new BufferedImage(gamePanel.getWidth(), gamePanel.getHeight(), BufferedImage.TYPE_INT_ARGB);
        bufferGraphics = buffer.createGraphics();
        frame.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                isFrameSizeSet = true;
                cellWidth = gamePanel.getWidth() / width;
                cellHeight = gamePanel.getHeight() / height;
                buffer = new BufferedImage(gamePanel.getWidth(), gamePanel.getHeight(), BufferedImage.TYPE_INT_ARGB);
                bufferGraphics = buffer.createGraphics();
                gamePanel.repaint();
            }
        });
        frame.setResizable(false);
        frame.setVisible(true);
        pacmanIcons = new ImageIcon[5];
        for (int i = 0; i < pacmanIcons.length; i++) {
            pacmanIcons[i] = new ImageIcon("resources/images/pac/pac" + i + ".png");
        }
        foodIcon = new ImageIcon("resources/images/food/0.png");
        specialFoodIcon = new ImageIcon("resources/images/food/4.png");
        rotatedPacmanIcons = new ImageIcon[pacmanIcons.length][4];
        for (int i = 0; i < pacmanIcons.length; i++) {
            for (int j = 0; j < 4; j++) {
                int angle = j * 90;
                BufferedImage image = new BufferedImage(pacmanIcons[i].getIconWidth(), pacmanIcons[i].getIconHeight(), BufferedImage.TYPE_INT_ARGB);
                Graphics g = image.createGraphics();
                pacmanIcons[i].paintIcon(null, g, 0, 0);
                g.dispose();
                BufferedImage rotatedImage = rotateImage(image, angle);
                rotatedPacmanIcons[i][j] = new ImageIcon(rotatedImage);
            }
        }
    }
    private ImageIcon getScaledGhostIcon(String path) {
        if (!isFrameSizeSet) {
            return null;
        }
        if (iconCache.containsKey(path)) {
            return iconCache.get(path);
        }
        ImageIcon newIcon = new ImageIcon(new ImageIcon(path).getImage().getScaledInstance(cellWidth, cellHeight, Image.SCALE_DEFAULT));
        iconCache.put(path, newIcon);
        return newIcon;
    }
    private ImageIcon getScaledIcon(int frame, int direction) {
        if (!isFrameSizeSet) {
            return rotatedPacmanIcons[frame][direction];
        }
        return new ImageIcon(rotatedPacmanIcons[frame][direction].getImage().getScaledInstance(cellWidth, cellHeight, Image.SCALE_DEFAULT));
    }
    public void updatePacmanPosition(int oldX, int oldY, int newX, int newY, int frame, int direction) {
        if (frame != -1) {
            int directionIndex;
            switch (direction) {
                case KeyEvent.VK_UP:
                    directionIndex = 3;
                    break;
                case KeyEvent.VK_DOWN:
                    directionIndex = 1;
                    break;
                case KeyEvent.VK_LEFT:
                    directionIndex = 2;
                    break;
                default:
                    directionIndex = 0;
                    break;
            }
            ImageIcon icon = getScaledIcon(frame, directionIndex);
            if (icon != null) {
                bufferGraphics.drawImage(icon.getImage(), newX * cellWidth, newY * cellHeight, cellWidth, cellHeight, null);
            }
        }
    }
    public void updateCell(int x, int y) {
        bufferGraphics.setColor(Color.BLACK);
        bufferGraphics.fillRect(x * cellWidth, y * cellHeight, cellWidth, cellHeight);
    }
    public void updateGhostPosition(Ghost ghost) {
        int x = ghost.getGhostX();
        int y = ghost.getGhostY();
        String imagePath = ghost.isWeakened() ? "resources/images/ghost/blue/1.png" : "resources/images/ghost/white/1.png";
        ImageIcon icon = getScaledGhostIcon(imagePath);
        if (icon != null) {
            bufferGraphics.drawImage(icon.getImage(), x * cellWidth, y * cellHeight, cellWidth, cellHeight, null);
        }
    }
    public void updateFoodPosition(int x, int y, int foodType) {
        ImageIcon foodImage = foodType == 2 ? specialFoodIcon : foodIcon;
        bufferGraphics.drawImage(foodImage.getImage(), x * cellWidth, y * cellHeight, cellWidth, cellHeight, null);
    }
    public BufferedImage rotateImage(BufferedImage image, double degrees) {
        double radians = Math.toRadians(degrees);
        double sin = Math.abs(Math.sin(radians));
        double cos = Math.abs(Math.cos(radians));
        int width = image.getWidth();
        int height = image.getHeight();
        int newWidth = (int) Math.floor(width * cos + height * sin);
        int newHeight = (int) Math.floor(height * cos + width * sin);
        BufferedImage rotatedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = rotatedImage.createGraphics();
        g2d.translate((newWidth - width) / 2, (newHeight - height) / 2);
        g2d.rotate(radians, width / 2, height / 2);
        g2d.drawRenderedImage(image, null);
        g2d.dispose();
        return rotatedImage;
    }
    public void drawMaze(PacmanModel model) {
        for (int i = 0; i < model.getMaze().length; i++) {
            for (int j = 0; j < model.getMaze()[i].length; j++) {
                bufferGraphics.setColor(model.getMaze()[i][j] ? Color.BLUE : Color.BLACK);
                bufferGraphics.fillRect(j * cellWidth, i * cellHeight, cellWidth, cellHeight);
                if (!model.getMaze()[i][j]) {
                    if (model.getFood()[i][j] == 1) {
                        bufferGraphics.drawImage(foodIcon.getImage(), j * cellWidth, i * cellHeight, cellWidth, cellHeight, null);
                    } else if (model.getFood()[i][j] == 2) {
                        bufferGraphics.drawImage(specialFoodIcon.getImage(), j * cellWidth, i * cellHeight, cellWidth, cellHeight, null);
                    }
                }
            }
        }
    }
    public boolean[][] getMaze() {
        boolean[][] maze = new boolean[labels.length][labels[0].length];
        for (int i = 0; i < labels.length; i++) {
            for (int j = 0; j < labels[i].length; j++) {
                maze[i][j] = labels[i][j].getBackground() == Color.BLUE;
            }
        }
        return maze;
    }
    public void clearBuffer() {
        bufferGraphics.clearRect(0, 0, buffer.getWidth(), buffer.getHeight());
    }
    public void repaint() {
        frame.repaint();
    }
    public void updateScore(int score) {
        scoreLabel.setText("Score: " + score);
    }
    public void updateLife(int life) {
        lifeLabel.setText("Life: " + life);
    }
    public void updateTime(int time) {
        timeLabel.setText("Time: " + time);
    }
}
