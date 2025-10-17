public class DeluxeAcyclicSP {
    private final double[][] energy;
    private final int width;
    private final int height;
    private final boolean vertical;

    private final double[][] distTo;
    private final int[][] edgeTo;

    public DeluxeAcyclicSP(double[][] energy, int width, int height, boolean vertical) {
        this.energy = energy;
        this.width = width;
        this.height = height;
        this.vertical = vertical;

        if (vertical) {
            distTo = new double[height][width];
            edgeTo = new int[height][width];
            findVerticalSeam();
        }
        else {
            distTo = new double[height][width];
            edgeTo = new int[height][width];
            findHorizontalSeam();
        }
    }

    // Dynamic programming for vertical seams
    private void findVerticalSeam() {
        for (int x = 0; x < width; x++) {
            distTo[0][x] = energy[0][x];
        }

        for (int y = 1; y < height; y++) {
            for (int x = 0; x < width; x++) {
                distTo[y][x] = Double.POSITIVE_INFINITY;
                for (int dx = -1; dx <= 1; dx++) {
                    int prevX = x + dx;
                    if (prevX >= 0 && prevX < width) {
                        if (distTo[y][x] > distTo[y - 1][prevX] + energy[y][x]) {
                            distTo[y][x] = distTo[y - 1][prevX] + energy[y][x];
                            edgeTo[y][x] = prevX;
                        }
                    }
                }
            }
        }
    }

    // Dynamic programming for horizontal seams
    private void findHorizontalSeam() {
        for (int y = 0; y < height; y++) {
            distTo[y][0] = energy[y][0];
        }

        for (int x = 1; x < width; x++) {
            for (int y = 0; y < height; y++) {
                distTo[y][x] = Double.POSITIVE_INFINITY;
                for (int dy = -1; dy <= 1; dy++) {
                    int prevY = y + dy;
                    if (prevY >= 0 && prevY < height) {
                        if (distTo[y][x] > distTo[prevY][x - 1] + energy[y][x]) {
                            distTo[y][x] = distTo[prevY][x - 1] + energy[y][x];
                            edgeTo[y][x] = prevY;
                        }
                    }
                }
            }
        }
    }

    // Return the seam as an array
    public int[] pathTo() {
        if (vertical) return verticalPath();
        else return horizontalPath();
    }

    private int[] verticalPath() {
        int[] seam = new int[height];

        // find min in last row
        double minDist = Double.POSITIVE_INFINITY;
        int minIndex = 0;
        for (int x = 0; x < width; x++) {
            if (distTo[height - 1][x] < minDist) {
                minDist = distTo[height - 1][x];
                minIndex = x;
            }
        }

        // backtrack
        int x = minIndex;
        for (int y = height - 1; y >= 0; y--) {
            seam[y] = x;
            x = edgeTo[y][x];
        }
        return seam;
    }

    private int[] horizontalPath() {
        int[] seam = new int[width];

        // find min in last column
        double minDist = Double.POSITIVE_INFINITY;
        int minIndex = 0;
        for (int y = 0; y < height; y++) {
            if (distTo[y][width - 1] < minDist) {
                minDist = distTo[y][width - 1];
                minIndex = y;
            }
        }

        // backtrack
        int y = minIndex;
        for (int x = width - 1; x >= 0; x--) {
            seam[x] = y;
            y = edgeTo[y][x];
        }
        return seam;
    }
}
