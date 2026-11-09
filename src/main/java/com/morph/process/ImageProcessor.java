package com.morph.process;

import com.morph.config.ImageConfig;
import com.morph.config.ImageLimits;
import com.morph.geometry.ImageDimensions;
import com.morph.geometry.ImageScaler;
import com.morph.validation.ImageValidator;

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
    private final ImageDecoder decoder;

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
        this.decoder = new ImageDecoder(
                ImageLimits.defaults(),
                validator
        );
    }

    public File process() {

        BufferedImage source = null;
        BufferedImage resized = null;
        Throwable failure = null;

        try {
            validator.validateInput(input);
            validator.validateOutput(input, output, existingFilePolicy);

            source = decoder.decode(input);

            ImageDimensions dimensions = scaler.fit(
                    source.getWidth(),
                    source.getHeight(),
                    config.getWidth(),
                    config.getHeight()
            );

            resized = renderer.resize(
                    source,
                    dimensions,
                    config.getInterpolationMode()
            );

            fileWriter.write(
                    resized,
                    output,
                    config.getFormat(),
                    config.getQuality(),
                    existingFilePolicy
            );

            return output;
        }
        catch (IOException e) {
            failure = e;
            throw new UncheckedIOException(
                    "Image processing failed.",
                    e
            );
        }
        catch (RuntimeException | Error e) {
            failure = e;
            throw e;
        }
        finally {
            RuntimeException cleanupFailure = null;

            try {
                if (source != null) source.flush();
            }
            catch (RuntimeException e) {
                cleanupFailure = e;
            }

            try {
                if (resized != null) resized.flush();
            }
            catch (RuntimeException e) {
                if (cleanupFailure == null) cleanupFailure = e;
                else cleanupFailure.addSuppressed(e);
            }

            if (cleanupFailure != null) {
                if (failure != null) failure.addSuppressed(cleanupFailure);
                else throw cleanupFailure;
            }
        }
    }
}
