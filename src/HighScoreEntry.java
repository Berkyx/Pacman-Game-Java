import java.io.Serializable;

public class HighScoreEntry implements Serializable {
    private String name;
    private int score;
    private int time;

    public HighScoreEntry(String name, int score, int time) {
        this.name = name;
        this.score = score;
        this.time = time;
    }
    public String getName() {
        return name;
    }
    public int getScore() {
        return score;
    }
    public int getTime() {
        return time;
    }
    @Override
    public String toString() {
        return name + ": " + score + " Points - " + time + " Seconds";
    }
}
