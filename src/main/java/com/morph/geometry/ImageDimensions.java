package com.morph.geometry;

public final class ImageDimensions {
    private final int width;
    private final int height;

    public ImageDimensions(int width, int height) {
        validate(width, height);

        this.width = width;
        this.height = height;
    }

    private void validate(int width, int height) {
        if (width <= 0 || height <= 0)
            throw new IllegalArgumentException(
                    "Image dimensions must be positive."
            );
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
