package test.com.morph.process;

import com.morph.config.ImageConfig;
import com.morph.geometry.ImageDimensions;
import com.morph.geometry.ImageScaler;
import com.morph.validation.ImageValidator;

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
    private final ImageScaler scaler;
    private final ImageRenderer renderer;
    private final ImageValidator validator;
    private final ExistingFilePolicy existingFilePolicy;
    private final ImageFileWriter fileWriter;

    public ImageProcessor(File input,
                          File output,
                          ImageConfig config,
                          ExistingFilePolicy existingFilePolicy) {

        this.input = Objects.requireNonNull(input);
        this.output = Objects.requireNonNull(output);
        this.config = Objects.requireNonNull(config);
        this.existingFilePolicy = Objects.requireNonNull(existingFilePolicy);

        this.scaler = new ImageScaler();
        this.renderer = new ImageRenderer();
        this.validator = new ImageValidator();
        this.fileWriter = new ImageFileWriter();
    }

    public File process() {
        try {
            validator.validateInput(input);
            validator.validateOutput(input, output, existingFilePolicy);

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
        BufferedImage image = ImageIO.read(input);

        if (image == null)
            throw new IllegalArgumentException(
                    "Unsupported image format."
            );

        return image;
    }

    private void write(BufferedImage image) throws IOException {
        fileWriter.write(
                image,
                output,
                config.getFormat(),
                config.getQuality(),
                existingFilePolicy
        );
    }
}
