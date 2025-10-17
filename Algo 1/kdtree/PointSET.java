import edu.princeton.cs.algs4.Point2D;
import edu.princeton.cs.algs4.RectHV;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class PointSET {

    // construct an empty set of points
    private final Set<Point2D> points = new TreeSet<>();

    // is the set empty?
    public boolean isEmpty() {
        return points.isEmpty();
    }

    // number of points in the set
    public int size() {
        return points.size();
    }

    // add the point to the set (if it is not already in the set)
    public void insert(Point2D p) {
        if (p == null)
            throw new IllegalArgumentException();
        if (!contains(p)) {
            points.add(p);
        }
    }

    // does the set contain point p?
    public boolean contains(Point2D p) {
        if (p == null)
            throw new IllegalArgumentException();
        return points.contains(p);
    }

    // draw all points to standard draw
    public void draw() {
        for (Point2D p : points) {
            p.draw();
        }
    }

    // all points that are inside the rectangle (or on the boundary)
    public Iterable<Point2D> range(RectHV rect) {
        if (rect == null)
            throw new IllegalArgumentException();
        List<Point2D> answer = new LinkedList<>();
        for (Point2D p : points) {
            if (rect.contains(p)) {
                answer.add(p);
            }
        }
        return answer;
    }

    // a nearest neighbor in the set to point p; null if the set is empty
    public Point2D nearest(Point2D p) {
        if (isEmpty()) return null;
        double nearestPointDistance = Double.MAX_VALUE;
        Point2D nearestPoint = p;
        for (Point2D point : points) {
            if (p.distanceTo(point) < nearestPointDistance) {
                nearestPointDistance = p.distanceTo(point);
                nearestPoint = point;
            }
        }
        return nearestPoint;
    }
}



