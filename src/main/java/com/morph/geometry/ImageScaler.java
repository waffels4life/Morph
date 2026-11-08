package com.morph.geometry;

public final class ImageScaler {

    public ImageDimensions fit(
            int sourceWidth,
            int sourceHeight,
            int targetWidth,
            int targetHeight) {

        if (sourceWidth <= 0
                || sourceHeight <= 0
                || targetWidth <= 0
                || targetHeight <= 0)
            throw new IllegalArgumentException(
                    "All image dimensions must be positive."
            );

        double ratio = Math.min(
                (double) targetWidth / sourceWidth,
                (double) targetHeight / sourceHeight
        );

        int newWidth = Math.max(
                1, Math.min((int) Math.round(ratio * sourceWidth), targetWidth)
        );

        int newHeight = Math.max(
                1, Math.min((int) Math.round(ratio * sourceHeight), targetHeight)
        );

        return new ImageDimensions(newWidth, newHeight);
    }
}