import edu.princeton.cs.algs4.Picture;

public class SeamCarver {
    private int[][] pixels;   // [row][col]
    private double[][] energy;
    private int width;
    private int height;

    // create a seam carver object based on the given picture
    public SeamCarver(Picture picture) {
        validate(picture, "Picture can't be null");
        this.width = picture.width();
        this.height = picture.height();
        this.pixels = pixels(picture);
        this.energy = energy();
    }

    // current picture
    public Picture picture() {
        Picture picture = new Picture(width, height);
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                picture.setRGB(col, row, pixels[row][col]);
            }
        }
        return picture;
    }

    // width of current picture
    public int width() {
        return width;
    }

    // height of current picture
    public int height() {
        return height;
    }

    // energy of pixel at column x and row y
    public double energy(int x, int y) {
        validate(x, y);

        if (x == 0 || x == width - 1 || y == 0 || y == height - 1)
            return 1000;

        int deltaX = delta(pixels[y][x + 1], pixels[y][x - 1]);
        int deltaY = delta(pixels[y + 1][x], pixels[y - 1][x]);
        return Math.sqrt(deltaX + deltaY);
    }

    // sequence of indices for horizontal seam
    public int[] findHorizontalSeam() {
        return findSeam(energy, width, height, false);
    }

    // sequence of indices for vertical seam
    public int[] findVerticalSeam() {
        return findSeam(energy, width, height, true);
    }

    private int[] findSeam(double[][] energy, int width, int height, boolean vertical) {
        DeluxeAcyclicSP path = new DeluxeAcyclicSP(energy, width, height, vertical);
        int[] seam = new int[vertical ? height : width];
        int i = 0;
        for (int v : path.pathTo()) {
            seam[i++] = v;
        }
        return seam;
    }

    // remove horizontal seam from current picture
    public void removeHorizontalSeam(int[] seam) {
        validate(seam, width, height);

        int[][] tmpPixels = new int[height - 1][width];
        for (int col = 0; col < width; col++) {
            for (int row = 0; row < height - 1; row++) {
                if (row < seam[col]) {
                    tmpPixels[row][col] = pixels[row][col];
                }
                else {
                    tmpPixels[row][col] = pixels[row + 1][col];
                }
            }
        }
        pixels = tmpPixels;
        height--;   // only reduce height
        energy = energy();
    }

    // remove vertical seam from current picture
    public void removeVerticalSeam(int[] seam) {
        validate(seam, height, width);

        int[][] tmpPixels = new int[height][width - 1];
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width - 1; col++) {
                if (col < seam[row]) {
                    tmpPixels[row][col] = pixels[row][col];
                }
                else {
                    tmpPixels[row][col] = pixels[row][col + 1];
                }
            }
        }
        pixels = tmpPixels;
        width--;   // only reduce width
        energy = energy();
    }

    private int[][] pixels(Picture picture) {
        int[][] pixels = new int[picture.height()][picture.width()];
        for (int row = 0; row < picture.height(); row++) {
            for (int col = 0; col < picture.width(); col++) {
                pixels[row][col] = picture.getRGB(col, row);
            }
        }
        return pixels;
    }

    private double[][] energy() {
        double[][] energy = new double[height][width];
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                if (row == 0 || row == height - 1 || col == 0 || col == width - 1) {
                    energy[row][col] = 1000;
                }
                else {
                    energy[row][col] = energy(col, row);
                }
            }
        }
        return energy;
    }

    private int delta(int previous, int next) {
        int rx = Math.abs(get(previous, 16) - get(next, 16));
        int gx = Math.abs(get(previous, 8) - get(next, 8));
        int bx = Math.abs(get(previous, 0) - get(next, 0));
        return (rx * rx) + (gx * gx) + (bx * bx);
    }

    private int get(int rgb, int color) {
        return (rgb >> color) & 0xFF;
    }

    private void validate(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height)
            throw new IllegalArgumentException();
    }

    private void validate(Object ob, String logs) {
        if (ob == null)
            throw new IllegalArgumentException(logs);
    }

    // seam validation
    private void validate(int[] seam, int height, int width) {
        validate(seam, "seam can't be null");

        if (seam.length != height)
            throw new IllegalArgumentException(
                    "invalid seam length, expected " + height + " but got " + seam.length);

        if (width <= 1)
            throw new IllegalArgumentException();

        for (int i = 0; i < seam.length; i++) {
            if (seam[i] < 0 || seam[i] >= width)
                throw new IllegalArgumentException(
                        "entry " + seam[i] + " is not between 0 and " + (width - 1));

            if (i > 0 && Math.abs(seam[i] - seam[i - 1]) > 1)
                throw new IllegalArgumentException("adjacent entries differ by more than 1");
        }
    }
}
