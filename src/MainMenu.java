import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

public class MainMenu extends JFrame {
    private MainMenuContentPanel contentPanel;

    public MainMenu() {
        showMenu();
    }
    public void startGame() {
        SwingUtilities.invokeLater(() -> {
            int width = 50;
            int height = 50;
            PacmanModel model = new PacmanModel(width, height);
            PacmanView view = new PacmanView(width, height);
            PacmanController controller = new PacmanController(model, view, height, width, this);
            this.setVisible(false);
        });
    }
    public void showMenu() {
        SwingUtilities.invokeLater(() -> {
            setTitle("Pacman - Main Menu");
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            contentPanel = new MainMenuContentPanel();
            add(contentPanel);
            pack();
            setLocationRelativeTo(null);
            setVisible(true);
        });
    }
    private class MainMenuContentPanel extends JPanel {
        private Font customFont;
        private JLabel newGameLabel;
        private JLabel highScoresLabel;
        private JLabel exitLabel;
        public MainMenuContentPanel() {
            ImageIcon logo = new ImageIcon();
            try {
                logo = new ImageIcon(ImageIO.read(this.getClass().getResource("images/pacman_logo.png")));
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                customFont = Font.createFont(Font.TRUETYPE_FONT, this.getClass().getResource("fonts/crackman.ttf").openStream()).deriveFont(24f);
            } catch (IOException | FontFormatException e) {
                e.printStackTrace();
            }
            setLayout(new BorderLayout());
            setPreferredSize(new Dimension(500, 250));
            add(new JLabel(logo), BorderLayout.NORTH);
            setBackground(Color.BLACK);
            JPanel menuItemsPanel = new JPanel();
            menuItemsPanel.setLayout(new BoxLayout(menuItemsPanel, BoxLayout.Y_AXIS));
            menuItemsPanel.setOpaque(false);
            newGameLabel = createMenuItem("New Game");
            newGameLabel.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    startGame();
                }
            });
            highScoresLabel = createMenuItem("High Scores");
            highScoresLabel.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    new HighScoresWindow();
                }
            });
            exitLabel = createMenuItem("Exit");
            menuItemsPanel.add(newGameLabel);
            menuItemsPanel.add(highScoresLabel);
            menuItemsPanel.add(exitLabel);
            add(menuItemsPanel, BorderLayout.CENTER);
        }
        private JLabel createMenuItem(String text) {
            JLabel menuItem = new JLabel(text, SwingConstants.CENTER);
            menuItem.setFont(customFont);
            menuItem.setForeground(Color.YELLOW);
            menuItem.setAlignmentX(Component.CENTER_ALIGNMENT);
            menuItem.addMouseListener(new MenuItemMouseAdapter(menuItem));
            return menuItem;
        }
    }
    private class MenuItemMouseAdapter extends MouseAdapter {
        private final JLabel menuItem;
        public MenuItemMouseAdapter(JLabel menuItem) {
            this.menuItem = menuItem;
        }
        @Override
        public void mouseClicked(MouseEvent e) {
            if (menuItem.getText().equals("Exit")) {
                System.exit(0);
            }
        }
        @Override
        public void mouseEntered(MouseEvent e) {
            menuItem.setForeground(Color.BLUE);
        }
        @Override
        public void mouseExited(MouseEvent e) {
            menuItem.setForeground(Color.YELLOW);
        }
    }
    public static void main(String[] args) {
        new MainMenu();
    }
}
