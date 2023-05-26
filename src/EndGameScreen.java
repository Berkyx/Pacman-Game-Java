import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class EndGameScreen extends JFrame {
    private JTextField textField;
    private int score;
    private int gameTime;
    private HighScores highScores;

    public EndGameScreen(int score, int gameTime) {
        this.score = score;
        this.gameTime = gameTime;
        highScores = new HighScores();
        highScores.loadFromFile();
        setTitle("Game Over");
        setSize(300, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(4, 1));
        JLabel gameOverLabel = new JLabel("Game Over", SwingConstants.CENTER);
        JLabel scoreLabel = new JLabel("Score: " + score, SwingConstants.CENTER);
        JLabel timeLabel = new JLabel("Time: " + gameTime, SwingConstants.CENTER);
        textField = new JTextField();
        textField.addActionListener(new TextFieldListener());
        add(gameOverLabel);
        add(scoreLabel);
        add(timeLabel);
        add(textField);
        setVisible(true);
    }
    private class TextFieldListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String name = textField.getText();
            HighScoreEntry entry = new HighScoreEntry(name, score, gameTime);
            highScores.addEntry(entry);
            highScores.saveToFile();
            dispose();
        }
    }
}
