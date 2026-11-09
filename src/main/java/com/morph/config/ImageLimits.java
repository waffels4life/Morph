package com.morph.config;

public final class ImageLimits {

    private final int maxWidth;
    private final int maxHeight;
    private final long maxPixels;

    public ImageLimits(int maxWidth, int maxHeight, long maxPixels) {
        if (maxWidth <= 0)
            throw new IllegalArgumentException(
                    "Maximum width must be positive."
            );

        if (maxHeight <= 0)
            throw new IllegalArgumentException(
                    "Maximum height must be positive."
            );

        if (maxPixels <= 0)
            throw new IllegalArgumentException(
                    "Maximum pixels must be positive."
            );

        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
        this.maxPixels = maxPixels;
    }

    public int getMaxWidth() {
        return maxWidth;
    }

    public int getMaxHeight() {
        return maxHeight;
    }

    public long getMaxPixels() {
        return maxPixels;
    }

    public static ImageLimits defaults() {
        return new ImageLimits(10_000, 10_000, 40_000_000L);
    }
}