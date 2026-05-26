package kfclash.citylogic.model;

public final class Dimension {
    private final int width;
    private final int height;

    public Dimension(int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Dimension width and height must be positive");
        }
        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
