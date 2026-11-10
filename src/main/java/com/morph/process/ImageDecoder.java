package com.morph.process;

import com.morph.config.ImageLimits;
import com.morph.validation.ImageValidator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Objects;

public final class ImageDecoder {

    private final ImageLimits limits;
    private final ImageValidator validator;

    public ImageDecoder(ImageLimits limits, ImageValidator validator) {
        this.limits = Objects.requireNonNull(limits);
        this.validator = Objects.requireNonNull(validator);
    }

    public BufferedImage decode(File input) throws IOException {
        Objects.requireNonNull(input, "Input file cannot be null.");

        ImageInputStream imageInput = ImageIO.createImageInputStream(input);

        if (imageInput == null)
            throw new IOException(
                    "Could not create image input stream."
            );


        try (imageInput) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(imageInput);

            if (!readers.hasNext())
                throw new IllegalArgumentException(
                        "Unsupported image format."
                );

            ImageReader reader = readers.next();

            try {
                reader.setInput(imageInput, true, true);

                int width = reader.getWidth(0);
                int height = reader.getHeight(0);

                validator.validateDimensions(width, height, limits);

                BufferedImage image = reader.read(0);

                if (image == null)
                    throw new IllegalArgumentException(
                            "Could not decode image."
                    );

                return image;
            }
            finally {
                reader.dispose();
            }
        }
    }
}
