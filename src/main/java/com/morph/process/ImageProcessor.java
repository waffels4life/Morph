package com.morph.process;

import com.morph.config.ImageConfig;

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
    private final ImageConfig config;
    private final ImageEncoder encoder;

    public ImageProcessor(File input,
                          File output,
                          ImageConfig config) {
        this.input = Objects.requireNonNull(input);
        this.output = Objects.requireNonNull(output);
        this.config = Objects.requireNonNull(config);
        this.encoder = new ImageEncoder();
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
        if (!input.isFile())
            throw new IllegalArgumentException(
                    "Input file does not exist."
            );

        if (!input.canRead())
            throw new IllegalArgumentException(
                    "Input file is not readable."
            );

        BufferedImage image = ImageIO.read(input);

        if (image == null)
            throw new IllegalArgumentException(
                    "Unsupported image format."
            );

        return image;
    }

    private BufferedImage resize(BufferedImage source) {
        BufferedImage result = new BufferedImage(
                config.getWidth(),
                config.getHeight(),
                BufferedImage.TYPE_INT_RGB
        );

        Graphics2D graphics = result.createGraphics();

        try {
            graphics.setRenderingHint(
                    RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BICUBIC
            );

            graphics.drawImage(source, 0, 0,
                    config.getWidth(), config.getHeight(), null
            );
        }
        finally {
            graphics.dispose();
        }

        return result;
    }

    private void write(BufferedImage image) throws IOException {
        encoder.encode(
                image,
                output,
                config.getFormat(),
                config.getQuality()
        );
    }
}
