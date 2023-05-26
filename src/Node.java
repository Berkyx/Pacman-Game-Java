public class Node implements Comparable<Node> {
    public Node parent;
    public int x, y;
    public double g;
    public double h;

    Node(Node parent, int x, int y, double g, double h) {
        this.parent = parent;
        this.x = x;
        this.y = y;
        this.g = g;
        this.h = h;
    }
    @Override
    public int compareTo(Node other) {
        return Double.compare(this.g + this.h, other.g + other.h);
    }
}
