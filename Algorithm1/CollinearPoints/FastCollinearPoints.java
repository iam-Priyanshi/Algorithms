import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdDraw;
import edu.princeton.cs.algs4.StdOut;

import java.util.ArrayList;
import java.util.Arrays;

public class FastCollinearPoints {
    private final LineSegment[] segments;

    public FastCollinearPoints(Point[] points) {
        if (points == null)
            throw new IllegalArgumentException("Input is null.");
        for (int i = 0; i < points.length; i++) {
            if (points[i] == null)
                throw new IllegalArgumentException("Input cotains null.");
        }
        Point[] localPoints = points.clone();

        Arrays.sort(localPoints);

        if (localPoints.length > 1) {
            for (int m = 1; m < localPoints.length; m++) {
                if (localPoints[m].compareTo(localPoints[m - 1]) == 0)
                    throw new IllegalArgumentException("Input contains duplicate.");
            }
        }
        ArrayList<LineSegment> res = new ArrayList<LineSegment>();

        if (localPoints.length > 3) {
            Point[] temp = localPoints.clone();
            for (Point p : localPoints) {
                Arrays.sort(temp, p.slopeOrder());
                findSegments(temp, p, res);
            }
        }

        segments = res.toArray(new LineSegment[res.size()]);
    }

    public int numberOfSegments() {
        return segments.length;
    }

    public LineSegment[] segments() {
        return segments.clone();
    }

    private void findSegments(Point[] points, Point p, ArrayList<LineSegment> res) {
        int start = 1;
        double slop = p.slopeTo(points[1]);

        for (int i = 2; i < points.length; i++) {
            double tempSlop = p.slopeTo(points[i]);
            if (!collinearSlop(tempSlop, slop)) {
                if (i - start >= 3) {
                    Point[] ls = genSegment(points, p, start, i);
                    if (ls[0].compareTo(p) == 0) {
                        res.add(new LineSegment(ls[0], ls[1]));
                    }
                }
                start = i;
                slop = tempSlop;
            }
        }
        if (points.length - start >= 3) {
            Point[] lastPoints = genSegment(points, p, start, points.length);
            if (lastPoints[0].compareTo(p) == 0) {
                res.add(new LineSegment(lastPoints[0], lastPoints[1]));
            }
        }
    }

    private boolean collinearSlop(double tempSlop, double slop) {
        if (Double.compare(slop, tempSlop) == 0)
            return true;
        return false;
    }

    private Point[] genSegment(Point[] points, Point p, int start, int end) {
        ArrayList<Point> temp = new ArrayList<>();
        temp.add(p);
        for (int i = start; i < end; i++) {
            temp.add(points[i]);
        }
        temp.sort(null);
        return new Point[] { temp.get(0), temp.get(temp.size() - 1) };
    }

    public static void main(String[] args) {
        In in = new In(args[0]);
        int n = in.readInt();
        Point[] points = new Point[n];
        for (int i = 0; i < n; i++) {
            int x = in.readInt();
            int y = in.readInt();
            points[i] = new Point(x, y);
        }

        StdDraw.enableDoubleBuffering();
        StdDraw.setXscale(0, 32768);
        StdDraw.setYscale(0, 32768);
        for (Point p : points) {
            p.draw();
        }
        StdDraw.show();

        FastCollinearPoints collinear = new FastCollinearPoints(points);
        for (LineSegment segment : collinear.segments()) {
            StdOut.println(segment);
            segment.draw();
        }
        StdDraw.show();
    }
}
