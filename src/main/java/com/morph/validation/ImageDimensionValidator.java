package com.morph.validation;

import com.morph.config.ImageLimits;

import java.util.Objects;

public final class ImageDimensionValidator {

    private final ImageLimits limits;

    public ImageDimensionValidator(ImageLimits limits) {
        this.limits = Objects.requireNonNull(
                limits, "Image limits cannot be null."
        );
    }

    public void validate(int width, int height) {
        if (width <= 0 || height <= 0)
            throw new IllegalArgumentException(
                    "Image dimensions must be positive."
            );

        if (width > limits.getMaxWidth())
            throw new IllegalArgumentException(
                    "Image width exceeds the maximum allowed width."
            );

        if (height > limits.getMaxHeight())
            throw new IllegalArgumentException(
                    "Image height exceeds the maximum allowed height."
            );

        long pixels = (long) width * height;

        if (pixels > limits.getMaxPixels())
            throw new IllegalArgumentException(
                    "Image pixel count exceeds the maximum allowed limit."
            );
    }
}
