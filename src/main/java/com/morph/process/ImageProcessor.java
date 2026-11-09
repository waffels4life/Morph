package com.morph.process;

import com.morph.config.ImageConfig;
import com.morph.geometry.ImageDimensions;
import com.morph.geometry.ImageScaler;

import javax.imageio.ImageIO;
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
    private final ImageScaler scaler;
    private final ImageRenderer renderer;

    public ImageProcessor(File input,
                          File output,
                          ImageConfig config) {

        this.input = Objects.requireNonNull(input);
        this.output = Objects.requireNonNull(output);
        this.config = Objects.requireNonNull(config);

        this.encoder = new ImageEncoder();
        this.scaler = new ImageScaler();
        this.renderer = new ImageRenderer();
    }

    public File process() {
        try {
            BufferedImage source = read();

            ImageDimensions dimensions = scaler.fit(
                    source.getWidth(),
                    source.getHeight(),
                    config.getWidth(),
                    config.getHeight()
            );

            BufferedImage resized = renderer.resize(
                    source,
                    dimensions,
                    config.getInterpolationMode()
            );
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

    private void write(BufferedImage image) throws IOException {
        encoder.encode(
                image,
                output,
                config.getFormat(),
                config.getQuality()
        );
    }
}
