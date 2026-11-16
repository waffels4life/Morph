package com.morph.io;

import com.morph.codec.encoder.EncoderRegistry;
import com.morph.format.ImageFormat;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public final class ImageFileWriter {

    private final EncoderRegistry encoderRegistry;
    private final AtomicMovePolicy atomicMovePolicy;
    private final FileMover fileMover;

    public ImageFileWriter(
            EncoderRegistry encoderRegistry,
            AtomicMovePolicy atomicMovePolicy,
            FileMover fileMover
    ) {
        this.encoderRegistry = Objects.requireNonNull(encoderRegistry);
        this.atomicMovePolicy = Objects.requireNonNull(atomicMovePolicy);
        this.fileMover = Objects.requireNonNull(fileMover);
    }

    public void write(
            BufferedImage image,
            File output,
            ImageFormat format,
            float quality,
            ExistingFilePolicy policy
    ) throws IOException {

        Objects.requireNonNull(image, "Image cannot be null.");
        Objects.requireNonNull(output, "Output cannot be null.");
        Objects.requireNonNull(format, "Format cannot be null.");
        Objects.requireNonNull(policy, "Policy cannot be null.");

        Path outputPath = output.toPath().toAbsolutePath();
        Path parent = outputPath.getParent();

        Path temp = Files.createTempFile(parent, "morph-", ".tmp");
        Throwable failure = null;

        try {
            encoderRegistry.getEncoder(format).encode(
                    image,
                    temp.toFile(),
                    quality
            );

            switch (policy) {
                case FAIL_IF_EXISTS -> Files.createLink(outputPath, temp);
                case REPLACE_EXISTING -> moveReplacing(temp, outputPath);
            }

        }
        catch (IOException | RuntimeException | Error e) {
            failure = e;
            throw e;
        }
        finally {
            try {
                Files.deleteIfExists(temp);
            }
            catch (IOException cleanupException) {
                if (failure != null) failure.addSuppressed(cleanupException);
                else throw cleanupException;
            }
        }
    }

    private void moveReplacing(Path source, Path target) throws IOException {
        try {
            fileMover.moveAtomically(source, target);
        }
        catch (AtomicMoveNotSupportedException e) {
            if (atomicMovePolicy == AtomicMovePolicy.REQUIRE_ATOMIC)
                throw e;

            fileMover.moveNormally(source, target);
        }
    }
}