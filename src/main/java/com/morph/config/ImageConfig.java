package com.morph.config;

import com.morph.format.ImageFormat;

import java.util.Objects;

public class ImageConfig {
    private final int width;
    private final int height;
    private final ImageFormat format;
    private final float quality;

    public ImageConfig(int width,
                       int height,
                       ImageFormat format,
                       float quality) {

        validate(width, height, format, quality);
        this.width = width;
        this.height = height;
        this.format = format;
        this.quality = quality;
    }

    private void validate(int width, int height, ImageFormat format, float quality) {
        Objects.requireNonNull(format);

        if (width <= 0 || height <= 0)
            throw new IllegalArgumentException(
                    "Image dimensions must be positive."
            );

        if (!Float.isFinite(quality)
                || Float.isNaN(quality)
                || quality < 0.0f
                || quality > 1.0f)
            throw new IllegalArgumentException(
                    "Invalid image quality."
            );

        if (format != ImageFormat.JPEG && quality != 1.0f)
            throw new IllegalArgumentException(
                    "Quality configuration is only supported for JPEG."
            );
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public ImageFormat getFormat() {
        return format;
    }

    public float getQuality() {
        return quality;
    }
}
