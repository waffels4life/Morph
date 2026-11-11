package com.morph.process;

import com.morph.codec.ImageDecoder;
import com.morph.config.ImageConfig;
import com.morph.config.InterpolationMode;
import com.morph.exception.InvalidImageException;
import com.morph.exception.MorphIOException;
import com.morph.format.ImageFormat;
import com.morph.io.ExistingFilePolicy;
import com.morph.io.ImageFileWriter;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
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

        ImageConfig config = ImageConfig.builder()
                .width(800)
                .height(400)
                .format(ImageFormat.PNG)
                .quality(1.0f)
                .interpolationMode(InterpolationMode.BICUBIC)
                .build();

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
        assertEquals(711, processed.getWidth());
        assertEquals(400, processed.getHeight());
    }


    @Test
    void shouldThrowExceptionWhenInputDoesNotExist() {

        // Arrange
        File input = tempDir.resolve("missing.png").toFile();
        File output = tempDir.resolve("output.png").toFile();

        ImageConfig config = ImageConfig.builder()
                .width(800)
                .height(600)
                .format(ImageFormat.PNG)
                .quality(1.0f)
                .interpolationMode(InterpolationMode.BICUBIC)
                .build();

        ImageProcessor processor = new ImageProcessor(
                input,
                output,
                config,
                ExistingFilePolicy.FAIL_IF_EXISTS
        );

        // Act & Assert
        assertThrows(
                MorphIOException.class,
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

        ImageConfig config = ImageConfig.builder()
                .width(800)
                .height(600)
                .format(ImageFormat.PNG)
                .quality(1.0f)
                .interpolationMode(InterpolationMode.BICUBIC)
                .build();

        ImageProcessor processor = new ImageProcessor(
                input,
                output,
                config,
                ExistingFilePolicy.FAIL_IF_EXISTS
        );

        // Act & Assert
        assertThrows(
                InvalidImageException.class,
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

        ImageConfig config = ImageConfig.builder()
                .width(800)
                .height(600)
                .format(ImageFormat.PNG)
                .quality(1.0f)
                .interpolationMode(InterpolationMode.BICUBIC)
                .build();

        ImageProcessor processor = new ImageProcessor(
                input,
                output,
                config,
                ExistingFilePolicy.FAIL_IF_EXISTS
        );

        // Act & Assert
        assertThrows(
                MorphIOException.class,
                processor::process
        );

        assertArrayEquals(
                originalContent,
                Files.readAllBytes(output.toPath())
        );
    }


    @ParameterizedTest
    @EnumSource(ImageFormat.class)
    void shouldSupportAllImageFormats(ImageFormat format) throws IOException {

        // Arrange
        File input = tempDir.resolve("input.png").toFile();

        File output = tempDir.resolve(
                "output." + format.getExtension()
        ).toFile();

        BufferedImage source = new BufferedImage(
                1920,
                1080,
                BufferedImage.TYPE_INT_RGB
        );

        ImageIO.write(source, "png", input);

        float quality = format == ImageFormat.JPEG ? 0.8f : 1.0f;

        ImageConfig config = ImageConfig.builder()
                .width(800)
                .height(600)
                .format(ImageFormat.PNG)
                .quality(1.0f)
                .interpolationMode(InterpolationMode.BICUBIC)
                .build();

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

        BufferedImage decoded = ImageIO.read(output);

        assertNotNull(decoded);
        assertEquals(800, decoded.getWidth());
        assertEquals(450, decoded.getHeight());
    }


    @Test
    void shouldPreserveTransparencyWhenProcessingPng() throws IOException {

        // Arrange
        File input = tempDir.resolve("transparent.png").toFile();
        File output = tempDir.resolve("output.png").toFile();

        BufferedImage source = new BufferedImage(
                100,
                100,
                BufferedImage.TYPE_INT_ARGB
        );

        source.setRGB(
                50,
                50,
                new java.awt.Color(255, 0, 0, 128).getRGB()
        );

        ImageIO.write(source, "png", input);

        ImageConfig config = ImageConfig.builder()
                .width(200)
                .height(200)
                .format(ImageFormat.PNG)
                .quality(1.0f)
                .interpolationMode(InterpolationMode.BICUBIC)
                .build();

        ImageProcessor processor = new ImageProcessor(
                input,
                output,
                config,
                ExistingFilePolicy.FAIL_IF_EXISTS
        );

        // Act
        processor.process();

        BufferedImage result = ImageIO.read(output);

        // Assert
        assertNotNull(result);
        assertTrue(result.getColorModel().hasAlpha());

        int alpha = (result.getRGB(100, 100) >>> 24) & 0xFF;

        assertTrue(alpha > 0 && alpha < 255);
    }


    @Test
    void shouldApplyJpegCompressionQuality() throws IOException {

        // Arrange
        File input = tempDir.resolve("input.png").toFile();

        File lowQualityOutput = tempDir.resolve("low.jpg").toFile();
        File highQualityOutput = tempDir.resolve("high.jpg").toFile();

        BufferedImage source = getBufferedImage();

        ImageIO.write(source, "png", input);

        ImageConfig lowQualityConfig = ImageConfig.builder()
                .width(1000)
                .height(1000)
                .format(ImageFormat.JPEG)
                .quality(0.2f)
                .interpolationMode(InterpolationMode.BICUBIC)
                .build();

        ImageConfig highQualityConfig = ImageConfig.builder()
                .width(1000)
                .height(1000)
                .format(ImageFormat.JPEG)
                .quality(1.0f)
                .interpolationMode(InterpolationMode.BICUBIC)
                .build();

        // Act
        new ImageProcessor(
                input, lowQualityOutput, lowQualityConfig,
                ExistingFilePolicy.FAIL_IF_EXISTS
        ).process();

        new ImageProcessor(
                input, highQualityOutput, highQualityConfig,
                ExistingFilePolicy.FAIL_IF_EXISTS
        ).process();

        // Assert
        assertTrue(lowQualityOutput.exists());
        assertTrue(highQualityOutput.exists());

        assertTrue(
                lowQualityOutput.length() < highQualityOutput.length(),
                "Low-quality JPEG should generally be smaller."
        );
    }

    private static @NonNull BufferedImage getBufferedImage() {
        BufferedImage source = new BufferedImage(
                1000,
                1000,
                BufferedImage.TYPE_INT_RGB
        );

        // Create a detailed image with varying pixel values.
        for (int y = 0; y < source.getHeight(); y++) {
            for (int x = 0; x < source.getWidth(); x++) {
                int red = (x * 255) / source.getWidth();
                int green = (y * 255) / source.getHeight();
                int blue = (x * y) % 256;

                source.setRGB(
                        x, y,
                        new java.awt.Color(red, green, blue).getRGB()
                );
            }
        }
        return source;
    }


    @Test
    void shouldProcessImageUsingInjectedDecoder() throws IOException {
        File input = tempDir.resolve("input.fake").toFile();
        File output = tempDir.resolve("output.png").toFile();

        Files.createFile(input.toPath());

        BufferedImage source = new BufferedImage(
                200, 100, BufferedImage.TYPE_INT_RGB
        );

        ImageDecoder decoder = new FakeImageDecoder(source);

        ImageConfig config = ImageConfig.builder()
                .width(100)
                .height(100)
                .format(ImageFormat.PNG)
                .quality(1.0f)
                .interpolationMode(InterpolationMode.BICUBIC)
                .build();

        ImageProcessor processor = new ImageProcessor(
                input,
                output,
                config,
                ExistingFilePolicy.FAIL_IF_EXISTS,
                decoder,
                new ImageRenderer(),
                new ImageFileWriter()
        );

        processor.process();

        BufferedImage result = ImageIO.read(output);

        assertNotNull(result);
        assertEquals(100, result.getWidth());
        assertEquals(50, result.getHeight());

        result.flush();
    }

}
