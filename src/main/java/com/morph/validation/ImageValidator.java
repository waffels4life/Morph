package com.morph.validation;

import com.morph.config.ImageLimits;
import com.morph.io.ExistingFilePolicy;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystemException;
import java.util.Objects;

public final class ImageValidator {

    public void validateInput(File input) throws FileNotFoundException, FileSystemException {
        Objects.requireNonNull(input, "Input must not be null.");

        if (!input.exists())
            throw new FileNotFoundException(
                    "Input file does not exist."
            );


        if (!input.isFile())
            throw new IllegalArgumentException(
                    "Input is not a regular file."
            );


        if (!input.canRead())
            throw new FileSystemException(
                    input.getAbsolutePath(),
                    null,
                    "Input file is not readable."
            );
    }

    public void validateOutput(File input, File output, ExistingFilePolicy policy) throws FileSystemException {
        Objects.requireNonNull(input, "Input must not be null.");
        Objects.requireNonNull(output, "Output must not be null.");
        Objects.requireNonNull(policy, "Exist policy must not be null.");

        if (input.toPath().toAbsolutePath().normalize().equals(
                output.toPath().toAbsolutePath().normalize()
        ))
            throw new IllegalArgumentException(
                    "Input and output must be different files."
            );

        File parent = output.getAbsoluteFile().getParentFile();

        if (!parent.exists())
            throw new FileSystemException(
                    parent.getAbsolutePath(),
                    null,
                    "Output directory does not exist."
            );

        if (!parent.isDirectory())
            throw new FileSystemException(
                    parent.getAbsolutePath(),
                    null,
                    "Output parent is not a directory."
            );

        if (!parent.canWrite())
            throw new FileSystemException(
                    parent.getAbsolutePath(),
                    null,
                    "Output directory is not writable."
            );

        if (output.exists()) {
            switch (policy) {
                case FAIL_IF_EXISTS ->
                        throw new FileAlreadyExistsException(
                                output.getAbsolutePath()
                        );

                case REPLACE_EXISTING -> {
                    if (!output.isFile())
                        throw new FileSystemException(
                                output.getAbsolutePath(),
                                null,
                                "Output is not a regular file."
                        );

                    if (!output.canWrite())
                        throw new FileSystemException(
                                output.getAbsolutePath(),
                                null,
                                "Output file is not writable."
                        );
                }
            }
        }
    }

    public void validateDimensions(int width, int height,
                                   ImageLimits limits) {

        Objects.requireNonNull(limits, "Image limits cannot be null.");

        if (width <= 0 || height <= 0)
            throw new IllegalArgumentException(
                    "Image dimensions must be positive."
            );


        if (width > limits.getMaxWidth())
            throw new IllegalArgumentException(
                    "Image width exceeds the maximum allowed width."
            );

        if (height > limits.getMaxHeight())
            throw new IllegalArgumentException(
                    "Image height exceeds the maximum allowed height."
            );

        long pixels = (long) width * height; // to prevent overflow

        if (pixels > limits.getMaxPixels())
            throw new IllegalArgumentException(
                    "Image pixel count exceeds the maximum allowed limit."
            );
    }
}
