
package com.morph.process;

import com.morph.config.ImageConfig;
import com.morph.config.InterpolationMode;
import com.morph.format.ImageFormat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ImageProcessorTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldProcessImageSuccessfully() throws IOException {

        // Arrange
        File input = tempDir.resolve("input.png").toFile();
        File output = tempDir.resolve("output.png").toFile();

        BufferedImage source = new BufferedImage(
                1920,
                1080,
                BufferedImage.TYPE_INT_RGB
        );

        ImageIO.write(source, "png", input);

        ImageConfig config = new ImageConfig(
                800,
                600,
                ImageFormat.PNG,
                1.0f,
                InterpolationMode.BICUBIC
        );

        ImageProcessor processor = new ImageProcessor(
                input,
                output,
                config,
                ExistingFilePolicy.FAIL_IF_EXISTS
        );

        // Act
        File result = processor.process();

        // Assert
        assertNotNull(result);
        assertTrue(output.exists());
        assertTrue(output.isFile());

        BufferedImage processed = ImageIO.read(output);

        assertNotNull(processed);
        assertEquals(800, processed.getWidth());
        assertEquals(450, processed.getHeight());
    }


    @Test
    void shouldThrowExceptionWhenInputDoesNotExist() {

        // Arrange
        File input = tempDir.resolve("missing.png").toFile();
        File output = tempDir.resolve("output.png").toFile();

        ImageConfig config = new ImageConfig(
                800,
                600,
                ImageFormat.PNG,
                1.0f,
                InterpolationMode.BICUBIC
        );

        ImageProcessor processor = new ImageProcessor(
                input,
                output,
                config,
                ExistingFilePolicy.FAIL_IF_EXISTS
        );

        // Act & Assert
        assertThrows(
                java.io.UncheckedIOException.class,
                processor::process
        );

        assertFalse(output.exists());
    }


    @Test
    void shouldThrowExceptionWhenInputFormatIsUnsupported() throws IOException {

        // Arrange
        File input = tempDir.resolve("invalid.png").toFile();
        File output = tempDir.resolve("output.png").toFile();

        Files.writeString(input.toPath(), "This is not an image.");

        ImageConfig config = new ImageConfig(
                800,
                600,
                ImageFormat.PNG,
                1.0f,
                InterpolationMode.BICUBIC
        );

        ImageProcessor processor = new ImageProcessor(
                input,
                output,
                config,
                ExistingFilePolicy.FAIL_IF_EXISTS
        );

        // Act & Assert
        assertThrows(
                IllegalArgumentException.class,
                processor::process
        );

        assertFalse(output.exists());
    }


    @Test
    void shouldNotOverwriteExistingOutputFile() throws IOException {

        // Arrange
        File input = tempDir.resolve("input.png").toFile();
        File output = tempDir.resolve("output.png").toFile();

        BufferedImage source = new BufferedImage(
                1920, 1080, BufferedImage.TYPE_INT_RGB
        );

        ImageIO.write(source, "png", input);

        byte[] originalContent = "Existing file".getBytes();
        Files.write(output.toPath(), originalContent);

        ImageConfig config = new ImageConfig(
                800,
                600,
                ImageFormat.PNG,
                1.0f,
                InterpolationMode.BICUBIC
        );

        ImageProcessor processor = new ImageProcessor(
                input,
                output,
                config,
                ExistingFilePolicy.FAIL_IF_EXISTS
        );

        // Act & Assert
        assertThrows(
                UncheckedIOException.class,
                processor::process
        );

        assertArrayEquals(
                originalContent,
                Files.readAllBytes(output.toPath())
        );
    }
}
