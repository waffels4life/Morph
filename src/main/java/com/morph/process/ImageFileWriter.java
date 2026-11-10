package com.morph.process;

import com.morph.format.ImageFormat;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

public final class ImageFileWriter {

    private final ImageEncoder encoder = new ImageEncoder();
    private final AtomicMovePolicy atomicMovePolicy;
    private final FileMover fileMover; // testing interface

    public ImageFileWriter() {
        this(AtomicMovePolicy.REQUIRE_ATOMIC, new NioFileMover());
    }

    public ImageFileWriter(AtomicMovePolicy atomicMovePolicy) {
        this(atomicMovePolicy, new NioFileMover());
    }

    public ImageFileWriter(AtomicMovePolicy atomicMovePolicy, FileMover fileMover) {
        this.atomicMovePolicy = Objects.requireNonNull(atomicMovePolicy);
        this.fileMover = Objects.requireNonNull(fileMover);
    }

    public void write(BufferedImage image,
                      File output,
                      ImageFormat format,
                      float quality,
                      ExistingFilePolicy policy) throws IOException {

        Objects.requireNonNull(image, "Image cannot be null.");
        Objects.requireNonNull(output, "Output cannot be null.");
        Objects.requireNonNull(format, "Format cannot be null.");
        Objects.requireNonNull(policy, "Policy cannot be null.");

        Path outputPath = output.toPath().toAbsolutePath();
        Path parent = outputPath.getParent();

        Path temp = Files.createTempFile(
                parent,
                "morph-",
                ".tmp"
        );

        Throwable failure = null;

        try {
            encoder.encode(
                    image,
                    temp.toFile(),
                    format,
                    quality
            );

            switch (policy) {
                case FAIL_IF_EXISTS -> Files.createLink(outputPath, temp);
                case REPLACE_EXISTING -> moveReplacing(outputPath, temp);
            }

        }
        catch (IOException
               | RuntimeException
               | Error e) {
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


    private void moveReplacing(Path output, Path temp) throws IOException {

        try {
            Files.move(
                    temp,
                    output,
                    StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING
            );

        }
        catch (AtomicMoveNotSupportedException e) {

            if (atomicMovePolicy == AtomicMovePolicy.REQUIRE_ATOMIC)
                throw e;

            Files.move(
                    temp,
                    output,
                    StandardCopyOption.REPLACE_EXISTING
            );
        }
    }
}