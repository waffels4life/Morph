package com.morph.codec;

import com.morph.config.ImageLimits;
import com.morph.exception.InvalidImageException;
import com.morph.validation.ImageValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ImageDecoderTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldDecodeValidImage() throws IOException {
        File input = tempDir.resolve("input.png").toFile();

        BufferedImage source = new BufferedImage(
                200, 100, BufferedImage.TYPE_INT_RGB
        );

        ImageIO.write(source, "png", input);

        ImageDecoder decoder = new ImageDecoder(
                ImageLimits.defaults(),
                new ImageValidator()
        );

        BufferedImage result = decoder.decode(input);

        assertNotNull(result);
        assertEquals(200, result.getWidth());
        assertEquals(100, result.getHeight());

        result.flush();
    }

    @Test
    void shouldThrowExceptionWhenInputIsNull() {
        ImageDecoder decoder = new ImageDecoder(
                ImageLimits.defaults(),
                new ImageValidator()
        );

        assertThrows(
                NullPointerException.class,
                () -> decoder.decode(null)
        );
    }

    @Test
    void shouldThrowExceptionWhenInputDoesNotExist() {
        File input = tempDir.resolve("missing.png").toFile();

        ImageDecoder decoder = new ImageDecoder(
                ImageLimits.defaults(),
                new ImageValidator()
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

        ImageDecoder decoder = new ImageDecoder(
                ImageLimits.defaults(),
                new ImageValidator()
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
                200, 200, BufferedImage.TYPE_INT_RGB
        );

        ImageIO.write(image, "png", input);

        ImageDecoder decoder = new ImageDecoder(
                new ImageLimits(500, 500, 30_000L),
                new ImageValidator()
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> decoder.decode(input)
        );
    }
}
