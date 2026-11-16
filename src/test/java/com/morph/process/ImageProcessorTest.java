package com.morph.process;

import com.morph.codec.CodecProviderLoader;
import com.morph.codec.decoder.ImageDecoder;
import com.morph.codec.decoder.ImageIoDecoder;
import com.morph.codec.encoder.EncoderRegistry;
import com.morph.config.ImageConfig;
import com.morph.config.ImageLimits;
import com.morph.config.InterpolationMode;
import com.morph.exception.InvalidImageException;
import com.morph.exception.MorphIOException;
import com.morph.format.ImageFormat;
import com.morph.io.AtomicMovePolicy;
import com.morph.io.ExistingFilePolicy;
import com.morph.io.ImageFileWriter;
import com.morph.io.NioFileMover;
import com.morph.validation.ImageDimensionValidator;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class ImageProcessorTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldProcessImageSuccessfully() throws IOException {

        File input = tempDir.resolve("input.png").toFile();
        File output = tempDir.resolve("output.png").toFile();

        BufferedImage source = new BufferedImage(
                1920,
                1080,
                BufferedImage.TYPE_INT_RGB
        );

        ImageIO.write(source, "png", input);

        ImageConfig config = createConfig(
                800,
                400,
                ImageFormat.PNG,
                1.0f
        );

        ImageProcessor processor = createProcessor(
                input,
                output,
                config,
                ExistingFilePolicy.FAIL_IF_EXISTS
        );

        File result = processor.process();

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

        File input = tempDir.resolve("missing.png").toFile();
        File output = tempDir.resolve("output.png").toFile();

        ImageConfig config = createConfig(
                800,
                600,
                ImageFormat.PNG,
                1.0f
        );

        ImageProcessor processor = createProcessor(
                input,
                output,
                config,
                ExistingFilePolicy.FAIL_IF_EXISTS
        );

        assertThrows(
                MorphIOException.class,
                processor::process
        );

        assertFalse(output.exists());
    }

    @Test
    void shouldThrowExceptionWhenInputFormatIsUnsupported() throws IOException {

        File input = tempDir.resolve("invalid.png").toFile();
        File output = tempDir.resolve("output.png").toFile();

        Files.writeString(input.toPath(), "This is not an image.");

        ImageConfig config = createConfig(
                800,
                600,
                ImageFormat.PNG,
                1.0f
        );

        ImageProcessor processor = createProcessor(
                input,
                output,
                config,
                ExistingFilePolicy.FAIL_IF_EXISTS
        );

        assertThrows(
                InvalidImageException.class,
                processor::process
        );

        assertFalse(output.exists());
    }

    @Test
    void shouldNotOverwriteExistingOutputFile() throws IOException {

        File input = tempDir.resolve("input.png").toFile();
        File output = tempDir.resolve("output.png").toFile();

        BufferedImage source = new BufferedImage(
                1920,
                1080,
                BufferedImage.TYPE_INT_RGB
        );

        ImageIO.write(source, "png", input);

        byte[] originalContent = "Existing file".getBytes();
        Files.write(output.toPath(), originalContent);

        ImageConfig config = createConfig(
                800,
                600,
                ImageFormat.PNG,
                1.0f
        );

        ImageProcessor processor = createProcessor(
                input,
                output,
                config,
                ExistingFilePolicy.FAIL_IF_EXISTS
        );

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
    @MethodSource("supportedFormats")
    void shouldSupportAllImageFormats(ImageFormat format) throws IOException {

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

        ImageConfig config = createConfig(
                800,
                600,
                format,
                quality
        );

        ImageProcessor processor = createProcessor(
                input,
                output,
                config,
                ExistingFilePolicy.FAIL_IF_EXISTS
        );

        File result = processor.process();

        assertNotNull(result);
        assertTrue(output.exists());

        BufferedImage decoded = ImageIO.read(output);

        assertNotNull(decoded);
        assertEquals(800, decoded.getWidth());
        assertEquals(450, decoded.getHeight());
    }

    static Stream<ImageFormat> supportedFormats() {
        return Stream.of(
                ImageFormat.JPEG,
                ImageFormat.PNG,
                ImageFormat.BMP
        );
    }

    @Test
    void shouldPreserveTransparencyWhenProcessingPng() throws IOException {

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

        ImageConfig config = createConfig(
                200,
                200,
                ImageFormat.PNG,
                1.0f
        );

        ImageProcessor processor = createProcessor(
                input,
                output,
                config,
                ExistingFilePolicy.FAIL_IF_EXISTS
        );

        processor.process();

        BufferedImage result = ImageIO.read(output);

        assertNotNull(result);
        assertTrue(result.getColorModel().hasAlpha());

        int alpha = (result.getRGB(100, 100) >>> 24) & 0xFF;

        assertTrue(alpha > 0 && alpha < 255);
    }

    @Test
    void shouldApplyJpegCompressionQuality() throws IOException {

        File input = tempDir.resolve("input.png").toFile();

        File lowQualityOutput = tempDir.resolve("low.jpg").toFile();
        File highQualityOutput = tempDir.resolve("high.jpg").toFile();

        BufferedImage source = getBufferedImage();

        ImageIO.write(source, "png", input);

        ImageConfig lowQualityConfig = createConfig(
                1000,
                1000,
                ImageFormat.JPEG,
                0.2f
        );

        ImageConfig highQualityConfig = createConfig(
                1000,
                1000,
                ImageFormat.JPEG,
                1.0f
        );

        createProcessor(
                input,
                lowQualityOutput,
                lowQualityConfig,
                ExistingFilePolicy.FAIL_IF_EXISTS
        ).process();

        createProcessor(
                input,
                highQualityOutput,
                highQualityConfig,
                ExistingFilePolicy.FAIL_IF_EXISTS
        ).process();

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

        for (int y = 0; y < source.getHeight(); y++) {
            for (int x = 0; x < source.getWidth(); x++) {

                int red = (x * 255) / source.getWidth();
                int green = (y * 255) / source.getHeight();
                int blue = (x * y) % 256;

                source.setRGB(
                        x,
                        y,
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
                200,
                100,
                BufferedImage.TYPE_INT_RGB
        );

        ImageDecoder decoder = new FakeImageDecoder(source);

        ImageConfig config = createConfig(
                100,
                100,
                ImageFormat.PNG,
                1.0f
        );

        ImageProcessor processor = createProcessor(
                input,
                output,
                config,
                ExistingFilePolicy.FAIL_IF_EXISTS,
                decoder
        );

        processor.process();

        BufferedImage result = ImageIO.read(output);

        assertNotNull(result);
        assertEquals(100, result.getWidth());
        assertEquals(50, result.getHeight());

        result.flush();
    }

    @Test
    void shouldCreateNewImageFormat() {
        ImageFormat format = new ImageFormat("webp", "webp");

        assertEquals("webp", format.getId());
        assertEquals("webp", format.getExtension());
    }

    @Test
    void shouldTreatDifferentInstancesWithSameIdAsEqual() {
        ImageFormat first = new ImageFormat("webp", "webp");
        ImageFormat second = new ImageFormat("webp", "webp");

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void shouldNormalizeIdAndExtensionBeforeComparison() {
        ImageFormat first = new ImageFormat("webp", "webp");
        ImageFormat second = new ImageFormat("WEBP", " WEBP ");

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void shouldRejectNullIDAsInput() {
        assertThrows(
                NullPointerException.class,
                () -> new ImageFormat(null, "webp")
        );
    }

    @Test
    void shouldRejectNullExtensionAsInput() {
        assertThrows(
                NullPointerException.class,
                () -> new ImageFormat("webp", null)
        );
    }

    @Test
    void shouldRejectEmptyIDAsInput() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new ImageFormat("", "webp")
        );
    }

    @Test
    void shouldRejectEmptyExtensionAsInput() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new ImageFormat("webp", "")
        );
    }

    @Test
    void shouldRejectWhiteSpaceIDAsInput() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new ImageFormat("   ", "webp")
        );
    }

    @Test
    void shouldRejectWhiteSpaceExtensionAsInput() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new ImageFormat("webp", "   ")
        );
    }

    @Test
    void shouldReturnIdAsString() {
        ImageFormat format = new ImageFormat("webp", "webp");

        assertEquals("webp", format.toString());
    }

    @Test
    void shouldReturnNormalizedStrings() {
        ImageFormat format = new ImageFormat(" Webp ", "WeBp");

        assertEquals("webp", format.getId());
        assertEquals("webp", format.getExtension());
    }

    private ImageProcessor createProcessor(
            File input,
            File output,
            ImageConfig config,
            ExistingFilePolicy policy
    ) {
        ImageDimensionValidator dimensionValidator =
                new ImageDimensionValidator(ImageLimits.defaults());

        ImageDecoder decoder = new ImageIoDecoder(dimensionValidator);

        return createProcessor(
                input,
                output,
                config,
                policy,
                decoder
        );
    }

    private ImageProcessor createProcessor(
            File input,
            File output,
            ImageConfig config,
            ExistingFilePolicy policy,
            ImageDecoder decoder
    ) {
        EncoderRegistry encoderRegistry = new EncoderRegistry(
                CodecProviderLoader.loadEncoderProviders()
        );

        ImageFileWriter fileWriter = new ImageFileWriter(
                encoderRegistry,
                AtomicMovePolicy.REQUIRE_ATOMIC,
                new NioFileMover()
        );

        return new ImageProcessor(
                input,
                output,
                config,
                policy,
                decoder,
                new ImageRenderer(),
                fileWriter
        );
    }

    private static ImageConfig createConfig(
            int width,
            int height,
            ImageFormat format,
            float quality
    ) {
        return ImageConfig.builder()
                .width(width)
                .height(height)
                .format(format)
                .quality(quality)
                .interpolationMode(InterpolationMode.BICUBIC)
                .build();
    }
}
