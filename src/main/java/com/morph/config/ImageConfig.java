package com.morph.config;

import com.morph.format.ImageFormat;

import java.util.Objects;

public final class ImageConfig {

    private final int width;
    private final int height;
    private final ImageFormat format;
    private final float quality;
    private final InterpolationMode interpolationMode;

    private ImageConfig(Builder builder) {
        validate(
                builder.width,
                builder.height,
                builder.format,
                builder.quality,
                builder.interpolationMode
        );

        this.width = builder.width;
        this.height = builder.height;
        this.format = builder.format;
        this.quality = builder.quality;
        this.interpolationMode = builder.interpolationMode;
    }

    public static Builder builder() {
        return new Builder();
    }

    private static void validate(
            int width,
            int height,
            ImageFormat format,
            float quality,
            InterpolationMode interpolationMode
    ) {
        Objects.requireNonNull(format, "format");
        Objects.requireNonNull(interpolationMode, "interpolationMode");

        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException(
                    "Image dimensions must be positive."
            );
        }

        if (!Float.isFinite(quality) || quality < 0.0f || quality > 1.0f) {
            throw new IllegalArgumentException("Invalid image quality.");
        }

        if (format != ImageFormat.JPEG && quality != 1.0f) {
            throw new IllegalArgumentException(
                    "Quality configuration is only supported for JPEG."
            );
        }
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

    public static final class Builder {

        private int width;
        private int height;
        private ImageFormat format;
        private float quality = 1.0f;
        private InterpolationMode interpolationMode =
                InterpolationMode.BICUBIC;

        private Builder() {
        }

        public Builder width(int width) {
            this.width = width;
            return this;
        }

        public Builder height(int height) {
            this.height = height;
            return this;
        }

        public Builder format(ImageFormat format) {
            this.format = format;
            return this;
        }

        public Builder quality(float quality) {
            this.quality = quality;
            return this;
        }

        public Builder interpolationMode(InterpolationMode mode) {
            this.interpolationMode = mode;
            return this;
        }

        public ImageConfig build() {
            return new ImageConfig(this);
        }
    }
}
