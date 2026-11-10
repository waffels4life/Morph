package com.morph.process;

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ImageFileWriterTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldNotFallbackWhenAtomicMoveIsRequired() throws IOException {

        // Arrange
        UnsupportedAtomicFileMover mover =
                new UnsupportedAtomicFileMover();

        ImageFileWriter writer = new ImageFileWriter(
                AtomicMovePolicy.REQUIRE_ATOMIC,
                mover
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

        ImageFileWriter writer = new ImageFileWriter(
                AtomicMovePolicy.ALLOW_NON_ATOMIC_FALLBACK,
                mover
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

        ImageFileWriter writer = new ImageFileWriter(
                AtomicMovePolicy.ALLOW_NON_ATOMIC_FALLBACK,
                mover
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

        ImageFileWriter writer = new ImageFileWriter();

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

        ImageFileWriter writer = new ImageFileWriter();

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
}
