package com.morph.process;

import com.morph.format.ImageFormat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
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
}
