package com.morph.process;

import com.morph.format.ImageFormat;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Objects;

public final class ImageEncoder implements ImageEncoding {

    @Override public void encode(BufferedImage image,
                       File output,
                       ImageFormat format,
                       float quality) throws IOException {

        Objects.requireNonNull(image, "Image cannot be null.");
        Objects.requireNonNull(output, "Output cannot be null.");
        Objects.requireNonNull(format, "Format cannot be null.");

        if (quality < 0.0f
                || quality > 1.0f
                || Float.isNaN(quality))
            throw new IllegalArgumentException(
                    "Invalid image quality."
            );

        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(
                format.getFormatName()
        );

        if (!writers.hasNext())
            throw new IllegalStateException(
                    "No suitable image writer found."
            );

        ImageWriter writer = writers.next();

        try {
            ImageWriteParam param = writer.getDefaultWriteParam();

            if (format == ImageFormat.JPEG) {
                if (!param.canWriteCompressed())
                    throw new IllegalStateException(
                            "Compression is not supported."
                    );

                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                param.setCompressionQuality(quality);
            }

            try (ImageOutputStream stream = ImageIO.createImageOutputStream(output)) {

                if (stream == null)
                    throw new IOException(
                            "Could not create image output stream."
                    );

                writer.setOutput(stream);

                writer.write(
                        null,
                        new IIOImage(image, null, null),
                        param
                );
            }
        }
        finally {
            writer.dispose();
        }
    }
}
