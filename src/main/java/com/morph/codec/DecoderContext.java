package com.morph.codec;

import com.morph.validation.ImageDimensionValidator;

import java.util.Objects;

public final class DecoderContext {

    private final ImageDimensionValidator dimensionValidator;

    public DecoderContext(ImageDimensionValidator dimensionValidator) {
        this.dimensionValidator = Objects.requireNonNull(
                dimensionValidator,
                "Dimension validator cannot be null."
        );
    }

    public ImageDimensionValidator getDimensionValidator() {
        return dimensionValidator;
    }
}
