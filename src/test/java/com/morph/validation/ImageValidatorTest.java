package com.morph.validation;

import com.morph.config.ImageLimits;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ImageValidatorTest {

    private final ImageValidator validator = new ImageValidator();

    @Test
    void shouldAcceptImageWithinLimits() {
        ImageLimits limits = new ImageLimits(10_000, 10_000, 40_000_000L);

        assertDoesNotThrow(() ->
                validator.validateDimensions(4000, 3000, limits));
    }

    @Test
    void shouldRejectWidthExceedingLimit() {
        ImageLimits limits = new ImageLimits(1000, 10_000, 40_000_000L);

        assertThrows(IllegalArgumentException.class, () ->
                validator.validateDimensions(1001, 500, limits));
    }

    @Test
    void shouldRejectHeightExceedingLimit() {
        ImageLimits limits = new ImageLimits(10_000, 1000, 40_000_000L);

        assertThrows(IllegalArgumentException.class, () ->
                validator.validateDimensions(500, 1001, limits));
    }

    @Test
    void shouldRejectPixelCountExceedingLimit() {
        ImageLimits limits = new ImageLimits(10_000, 10_000, 1_000_000L);

        assertThrows(IllegalArgumentException.class, () ->
                validator.validateDimensions(1500, 1000, limits));
    }

    @Test
    void shouldRejectZeroOrNegativeDimensions() {
        ImageLimits limits = ImageLimits.defaults();

        assertThrows(IllegalArgumentException.class, () ->
                validator.validateDimensions(0, 100, limits));

        assertThrows(IllegalArgumentException.class, () ->
                validator.validateDimensions(100, -1, limits));
    }
}
