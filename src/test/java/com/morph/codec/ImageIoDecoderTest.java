package com.morph.codec;

import com.morph.codec.decoder.ImageIoDecoder;
import com.morph.config.ImageLimits;
import com.morph.exception.InvalidImageException;
import com.morph.validation.ImageDimensionValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ImageIoDecoderTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldDecodeValidImage() throws IOException {
        File input = tempDir.resolve("input.png").toFile();

        BufferedImage source = new BufferedImage(
                200, 100, BufferedImage.TYPE_INT_RGB
        );

        ImageIO.write(source, "png", input);

        ImageDimensionValidator dimensionValidator =
                new ImageDimensionValidator(ImageLimits.defaults());

        ImageIoDecoder decoder = new ImageIoDecoder(
                dimensionValidator
        );

        BufferedImage result = decoder.decode(input);

        assertNotNull(result);
        assertEquals(200, result.getWidth());
        assertEquals(100, result.getHeight());

        result.flush();
    }

    @Test
    void shouldThrowExceptionWhenInputIsNull() {
        ImageDimensionValidator dimensionValidator =
                new ImageDimensionValidator(ImageLimits.defaults());

        ImageIoDecoder decoder = new ImageIoDecoder(
                dimensionValidator
        );

        assertThrows(
                NullPointerException.class,
                () -> decoder.decode(null)
        );
    }

    @Test
    void shouldThrowExceptionWhenInputDoesNotExist() {
        File input = tempDir.resolve("missing.png").toFile();

        ImageDimensionValidator dimensionValidator =
                new ImageDimensionValidator(ImageLimits.defaults());

        ImageIoDecoder decoder = new ImageIoDecoder(
                dimensionValidator
        );

        assertThrows(
                IOException.class,
                () -> decoder.decode(input)
        );
    }

    @Test
    void shouldRejectUnsupportedImageFormat() throws IOException {
        File input = tempDir.resolve("invalid.png").toFile();

        Files.writeString(input.toPath(), "This is not an image.");

        ImageDimensionValidator dimensionValidator =
                new ImageDimensionValidator(ImageLimits.defaults());

        ImageIoDecoder decoder = new ImageIoDecoder(
                dimensionValidator
        );

        assertThrows(
                InvalidImageException.class,
                () -> decoder.decode(input)
        );
    }

    @Test
    void shouldRejectImageExceedingPixelLimit() throws IOException {
        File input = tempDir.resolve("large.png").toFile();

        BufferedImage image = new BufferedImage(
                200,
                100,
                BufferedImage.TYPE_INT_RGB
        );

        ImageIO.write(image, "png", input);

        ImageLimits limits = new ImageLimits(500, 500, 10_000);

        ImageDimensionValidator validator =
                new ImageDimensionValidator(limits);

        ImageIoDecoder decoder = new ImageIoDecoder(validator);

        assertThrows(
                IllegalArgumentException.class,
                () -> decoder.decode(input)
        );
    }
}
