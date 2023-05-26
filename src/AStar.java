import java.awt.*;
import java.util.*;
import java.util.List;

public class AStar {
    public static List<Point> getPath(boolean[][] maze, Point start, Point goal) {
        PriorityQueue<Node> openList = new PriorityQueue<>();
        boolean[][] closedList = new boolean[maze.length][maze[0].length];
        Node startNode = new Node(null, start.x, start.y, 0, getDistance(start, goal));
        openList.add(startNode);
        Random random = new Random();
        while (!openList.isEmpty()) {
            Node currentNode = openList.poll();
            closedList[currentNode.y][currentNode.x] = true;
            if (currentNode.x == goal.x && currentNode.y == goal.y) {
                return buildPath(currentNode);
            }
            for (int[] direction : new int[][] {{-1, 0}, {1, 0}, {0, -1}, {0, 1}}) {
                int newX = currentNode.x + direction[0];
                int newY = currentNode.y + direction[1];
                if (newX >= 0 && newX < maze[0].length && newY >= 0 && newY < maze.length && !maze[newY][newX] && !closedList[newY][newX]) {
                    double extraCost = random.nextDouble(); // Add random cost
                    Node newNode = new Node(currentNode, newX, newY, currentNode.g + 1 * extraCost, getDistance(new Point(newX, newY), goal));
                    openList.add(newNode);
                }
            }
        }
        return Collections.emptyList();
    }
    private static double getDistance(Point a, Point b) {
        int dx = b.x - a.x;
        int dy = b.y - a.y;
        return Math.sqrt(dx * dx + dy * dy);
    }
    private static List<Point> buildPath(Node node) {
        List<Point> path = new ArrayList<>();
        while (node != null) {
            path.add(new Point(node.x, node.y));
            node = node.parent;
        }
        Collections.reverse(path);
        return path;
    }
}
