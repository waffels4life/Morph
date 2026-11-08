package com.morph.process;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Objects;

public final class ImageProcessor {

    private final File input;
    private final File output;
    private final int width;
    private final int height;

    public ImageProcessor(File input, File output,
                          int width, int height) {
        this.input = Objects.requireNonNull(input);
        this.output = Objects.requireNonNull(output);

        if (width <= 0 || height <= 0)
            throw new IllegalArgumentException(
                    "Invalid image dimensions."
            );

        this.width = width;
        this.height = height;
    }

    public File process() {
        try {
            BufferedImage source = read();
            BufferedImage resized = resize(source);
            write(resized);

            return output;
        }
        catch (IOException e) {
            throw new UncheckedIOException(
                    "Image processing failed.",
                    e
            );
        }
    }

    private BufferedImage read() throws IOException {
        BufferedImage image = ImageIO.read(input);

        if (image == null)
            throw new IllegalArgumentException(
                    "Unsupported image format."
            );

        return image;
    }

    private BufferedImage resize(BufferedImage source) {
        BufferedImage result = new BufferedImage(
                width,
                height,
                BufferedImage.TYPE_INT_RGB
        );

        Graphics2D graphics = result.createGraphics();

        try {
            graphics.setRenderingHint(
                    RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BICUBIC
            );

            graphics.drawImage(source, 0, 0, width, height, null);
        }
        finally {
            graphics.dispose();
        }

        return result;
    }

    private void write(BufferedImage image) throws IOException {
        boolean written = ImageIO.write(image, "jpg", output);

        if (!written)
            throw new IllegalStateException(
                    "No suitable image writer found."
            );
    }
}
