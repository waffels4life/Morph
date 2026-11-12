package com.morph.validation;

import com.morph.config.ImageLimits;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class ImageValidatorTest {

    private final ImageValidator validator = new ImageValidator();

    @Test
    void shouldAcceptImageWithinLimits() {
        ImageLimits limits = new ImageLimits(10_000, 10_000, 40_000_000L);

        ImageDimensionValidator validator =
                new ImageDimensionValidator(limits);

        assertDoesNotThrow(() ->
                validator.validate(4000, 3000)
        );
    }
}
