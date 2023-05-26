import javax.swing.*;
import java.util.List;

public class HighScoresWindow extends JFrame {
    private HighScores highScores;

    public HighScoresWindow() {
        highScores = new HighScores();
        highScores.loadFromFile();
        setTitle("High Scores");
        setSize(300, 200);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        List<HighScoreEntry> entries = highScores.getEntries();
        JList<HighScoreEntry> list = new JList<>(entries.toArray(new HighScoreEntry[0]));
        JScrollPane scrollPane = new JScrollPane(list);
        add(scrollPane);
        setVisible(true);
    }
}
