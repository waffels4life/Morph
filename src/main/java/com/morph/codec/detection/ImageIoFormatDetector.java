package com.morph.codec.detection;

import com.morph.exception.InvalidImageException;
import com.morph.format.ImageFormat;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Objects;

public final class ImageIoFormatDetector implements ImageFormatDetector {

    @Override public ImageFormat detect(File input) throws IOException {
        Objects.requireNonNull(input, "Input file cannot be null.");

        ImageInputStream stream = ImageIO.createImageInputStream(input);

        if (stream == null)
            throw new IOException(
                    "Could not create image input stream."
            );

        try (stream) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(stream);

            if (!readers.hasNext())
                throw new InvalidImageException(
                        "Unsupported image format."
                );

            ImageReader reader = readers.next();

            try {
                String formatName = reader.getFormatName();

                return new ImageFormat(formatName, formatName);
            }
            finally {
                reader.dispose();
            }
        }
    }
}
