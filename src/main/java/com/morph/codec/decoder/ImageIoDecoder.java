package com.morph.codec.decoder;

import com.morph.exception.InvalidImageException;
import com.morph.validation.ImageDimensionValidator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Objects;

public final class ImageIoDecoder implements ImageDecoder {

    private final ImageDimensionValidator dimensionValidator;

    public ImageIoDecoder(ImageDimensionValidator dimensionValidator) {
        this.dimensionValidator = Objects.requireNonNull(dimensionValidator);
    }

    @Override public BufferedImage decode(File input) throws IOException {
        Objects.requireNonNull(input, "Input file cannot be null.");

        ImageInputStream imageInput = ImageIO.createImageInputStream(input);

        if (imageInput == null)
            throw new IOException(
                    "Could not create image input stream."
            );


        try (imageInput) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(imageInput);

            if (!readers.hasNext())
                throw new InvalidImageException(
                        "Unsupported image format."
                );

            ImageReader reader = readers.next();

            try {
                reader.setInput(imageInput, true, true);

                int width = reader.getWidth(0);
                int height = reader.getHeight(0);

                dimensionValidator.validate(width, height);

                BufferedImage image = reader.read(0);

                if (image == null)
                    throw new InvalidImageException(
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
