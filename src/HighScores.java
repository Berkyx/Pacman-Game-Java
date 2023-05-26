import java.io.*;
import java.util.*;

public class HighScores implements Serializable {
    private static final String FILENAME = "highscores.ser";
    private List<HighScoreEntry> entries;

    public HighScores() {
        loadFromFile();
    }
    public void addEntry(HighScoreEntry entry) {
        entries.add(entry);
        sortEntries();
    }
    public List<HighScoreEntry> getEntries() {
        return entries;
    }
    public void saveToFile() {
        try {
            FileOutputStream fileOut = new FileOutputStream(FILENAME);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(entries);
            out.close();
            fileOut.close();
        } catch (IOException i) {
            i.printStackTrace();
        }
    }
    public void loadFromFile() {
        try {
            File file = new File(FILENAME);
            if (!file.exists()) {
                entries = new ArrayList<>();
                return;
            }

            FileInputStream fis = new FileInputStream(file);
            ObjectInputStream ois = new ObjectInputStream(fis);
            entries = (ArrayList<HighScoreEntry>) ois.readObject();
            sortEntries();
            ois.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    private void sortEntries() {
        Collections.sort(entries, new Comparator<HighScoreEntry>() {
            @Override
            public int compare(HighScoreEntry e1, HighScoreEntry e2) {
                return Integer.compare(e2.getScore(), e1.getScore());
            }
        });
    }
}
