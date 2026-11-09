package com.morph.process;

import com.morph.format.ImageFormat;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

public final class ImageFileWriter {

    private final ImageEncoder encoder = new ImageEncoder();

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

        try {
            encoder.encode(
                    image,
                    temp.toFile(),
                    format,
                    quality
            );

            switch (policy) {
                case FAIL_IF_EXISTS -> Files.createLink(outputPath, temp);
                case REPLACE_EXISTING -> Files.move(
                                temp,
                                outputPath,
                                StandardCopyOption.ATOMIC_MOVE
                        );
            }

        }
        finally {
            Files.deleteIfExists(temp);
        }
    }
}