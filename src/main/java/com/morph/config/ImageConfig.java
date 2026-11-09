package com.morph.config;

import com.morph.format.ImageFormat;

import java.util.Objects;

public final class ImageConfig {
    private final int width;
    private final int height;
    private final ImageFormat format;
    private final float quality;
    private final InterpolationMode interpolationMode;

    public ImageConfig(int width,
                       int height,
                       ImageFormat format,
                       float quality,
                       InterpolationMode interpolationMode) {


        validate(width, height, format, quality, interpolationMode);
        this.width = width;
        this.height = height;
        this.format = format;
        this.quality = quality;
        this.interpolationMode = interpolationMode;
    }

    private void validate(int width,
                          int height,
                          ImageFormat format,
                          float quality,
                          InterpolationMode interpolationMode) {

        Objects.requireNonNull(format);
        Objects.requireNonNull(interpolationMode);

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

    public InterpolationMode getInterpolationMode() {
        return interpolationMode;
    }
}
