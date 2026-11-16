package com.morph.io;

import com.morph.codec.FailingImageEncoder;
import com.morph.codec.encoder.EncoderRegistry;
import com.morph.codec.encoder.ImageEncoder;
import com.morph.codec.encoder.ImageEncoderProvider;
import com.morph.codec.encoder.ImageIoEncoder;
import com.morph.format.ImageFormat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ImageFileWriterTest {

    @TempDir
    Path tempDir;


    private ImageFileWriter createWriter(
            AtomicMovePolicy policy,
            FileMover mover,
            ImageEncoder encoder
    ) {
        ImageEncoderProvider provider = new ImageEncoderProvider() {

            @Override
            public Set<ImageFormat> getSupportedFormats() {
                return Set.of(ImageFormat.PNG);
            }

            @Override
            public ImageEncoder createEncoder(ImageFormat format) {
                if (format != ImageFormat.PNG)
                    throw new IllegalArgumentException("Unsupported format.");

                return encoder;
            }
        };

        EncoderRegistry registry = new EncoderRegistry(Set.of(provider));

        return new ImageFileWriter(registry, policy, mover);
    }


    @Test
    void shouldNotFallbackWhenAtomicMoveIsRequired() throws IOException {

        // Arrange
        UnsupportedAtomicFileMover mover =
                new UnsupportedAtomicFileMover();

        ImageFileWriter writer = createWriter(
                AtomicMovePolicy.REQUIRE_ATOMIC,
                new NioFileMover(),
                new ImageIoEncoder(ImageFormat.PNG)
        );

        BufferedImage image = new BufferedImage(
                100, 100, BufferedImage.TYPE_INT_RGB
        );

        Path output = tempDir.resolve("output.png");

        // Act & Assert
        assertThrows(
                AtomicMoveNotSupportedException.class,
                () -> writer.write(
                        image,
                        output.toFile(),
                        ImageFormat.PNG,
                        1.0f,
                        ExistingFilePolicy.REPLACE_EXISTING
                )
        );

        assertTrue(mover.wasAtomicMoveCalled());
        assertFalse(mover.wasNormalMoveCalled());
    }

    @Test
    void shouldFallbackWhenAtomicMoveIsAllowed() throws IOException {

        UnsupportedAtomicFileMover mover =
                new UnsupportedAtomicFileMover();

        ImageFileWriter writer = createWriter(
                AtomicMovePolicy.REQUIRE_ATOMIC,
                new NioFileMover(),
                new FailingImageEncoder()
        );

        BufferedImage image = new BufferedImage(
                100, 100, BufferedImage.TYPE_INT_RGB
        );

        Path output = tempDir.resolve("output.png");

        writer.write(
                image,
                output.toFile(),
                ImageFormat.PNG,
                1.0f,
                ExistingFilePolicy.REPLACE_EXISTING
        );

        assertTrue(mover.wasAtomicMoveCalled());
        assertTrue(mover.wasNormalMoveCalled());
        assertTrue(Files.exists(output));
    }

    final class FailingFileMover implements FileMover {

        private boolean normalMoveCalled;

        @Override public void moveAtomically(Path source, Path target)
                throws IOException {
            throw new IOException("Simulated disk failure.");
        }

        @Override public void moveNormally(Path source, Path target)
                throws IOException {
            normalMoveCalled = true;
        }

        boolean wasNormalMoveCalled() {
            return normalMoveCalled;
        }
    }

    @Test
    void shouldNotFallbackOnOtherIOException() throws IOException {

        FailingFileMover mover = new FailingFileMover();

        ImageFileWriter writer = createWriter(
                AtomicMovePolicy.REQUIRE_ATOMIC,
                new NioFileMover(),
                new ImageIoEncoder(ImageFormat.PNG)
        );

        BufferedImage image = new BufferedImage(
                100, 100, BufferedImage.TYPE_INT_RGB
        );

        Path output = tempDir.resolve("output.png");

        assertThrows(
                IOException.class,
                () -> writer.write(
                        image,
                        output.toFile(),
                        ImageFormat.PNG,
                        1.0f,
                        ExistingFilePolicy.REPLACE_EXISTING
                )
        );

        assertFalse(mover.wasNormalMoveCalled());
        assertFalse(Files.exists(output));
    }

    @Test
    void shouldNotOverwriteExistingFileWhenPolicyIsFailIfExists()
            throws IOException {

        ImageFileWriter writer = createWriter(
                AtomicMovePolicy.REQUIRE_ATOMIC,
                new NioFileMover(),
                new ImageIoEncoder(ImageFormat.PNG)
        );

        BufferedImage image = new BufferedImage(
                100, 100, BufferedImage.TYPE_INT_RGB
        );

        Path output = tempDir.resolve("existing.png");

        String originalContent = "Do not overwrite me.";
        Files.writeString(output, originalContent);

        assertThrows(
                FileAlreadyExistsException.class,
                () -> writer.write(
                        image,
                        output.toFile(),
                        ImageFormat.PNG,
                        1.0f,
                        ExistingFilePolicy.FAIL_IF_EXISTS
                )
        );

        assertEquals(
                originalContent,
                Files.readString(output)
        );
    }

    @Test
    void shouldReplaceExistingFileWhenPolicyIsReplaceExisting()
            throws IOException {

        ImageFileWriter writer = createWriter(
                AtomicMovePolicy.REQUIRE_ATOMIC,
                new NioFileMover(),
                new ImageIoEncoder(ImageFormat.PNG)
        );

        BufferedImage image = new BufferedImage(
                100, 100, BufferedImage.TYPE_INT_RGB
        );

        Path output = tempDir.resolve("existing.png");

        Files.writeString(output, "Old content");

        writer.write(
                image,
                output.toFile(),
                ImageFormat.PNG,
                1.0f,
                ExistingFilePolicy.REPLACE_EXISTING
        );

        assertTrue(Files.exists(output));
        assertTrue(Files.size(output) > 0);

        BufferedImage decoded = ImageIO.read(output.toFile());

        assertNotNull(decoded);
        assertEquals(100, decoded.getWidth());
        assertEquals(100, decoded.getHeight());

        decoded.flush();
    }

    @Test
    void shouldCleanupTemporaryFileWhenEncodingFails() throws IOException {

        FailingImageEncoder encoder = new FailingImageEncoder();

        ImageFileWriter writer = createWriter(
                AtomicMovePolicy.REQUIRE_ATOMIC,
                new NioFileMover(),
                new ImageIoEncoder(ImageFormat.PNG)
        );

        BufferedImage image = new BufferedImage(
                100, 100, BufferedImage.TYPE_INT_RGB
        );

        Path output = tempDir.resolve("output.png");

        assertThrows(
                IOException.class,
                () -> writer.write(
                        image,
                        output.toFile(),
                        ImageFormat.PNG,
                        1.0f,
                        ExistingFilePolicy.REPLACE_EXISTING
                )
        );

        assertFalse(Files.exists(output));

        try (var files = Files.list(tempDir)) {
            assertEquals(0, files.count());
        }
    }

    @Test
    void shouldPreserveExistingFileWhenEncodingFails() throws IOException {

        ImageFileWriter writer = createWriter(
                AtomicMovePolicy.REQUIRE_ATOMIC,
                new NioFileMover(),
                new ImageIoEncoder(ImageFormat.PNG)
        );

        BufferedImage image = new BufferedImage(
                100, 100, BufferedImage.TYPE_INT_RGB
        );

        Path output = tempDir.resolve("existing.png");

        byte[] originalContent = "Original content".getBytes();
        Files.write(output, originalContent);

        assertThrows(
                IOException.class,
                () -> writer.write(
                        image,
                        output.toFile(),
                        ImageFormat.PNG,
                        1.0f,
                        ExistingFilePolicy.REPLACE_EXISTING
                )
        );

        assertArrayEquals(
                originalContent,
                Files.readAllBytes(output)
        );
    }
}
