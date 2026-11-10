package com.morph.process;

import com.morph.codec.ImageIoDecoder;
import com.morph.config.ImageConfig;
import com.morph.config.ImageLimits;
import com.morph.exception.MorphIOException;
import com.morph.geometry.ImageDimensions;
import com.morph.geometry.ImageScaler;
import com.morph.io.ExistingFilePolicy;
import com.morph.io.ImageFileWriter;
import com.morph.validation.ImageValidator;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
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
    private final ImageIoDecoder decoder;

    public ImageProcessor(
            File input,
            File output,
            ImageConfig config,
            ExistingFilePolicy existingFilePolicy,
            ImageLimits limits
    ) {
        this(
                input,
                output,
                config,
                existingFilePolicy,
                new ImageIoDecoder(
                        limits,
                        new ImageValidator()
                ),
                new ImageRenderer(),
                new ImageFileWriter()
        );
    }


    public ImageProcessor(
            File input,
            File output,
            ImageConfig config,
            ExistingFilePolicy existingFilePolicy,
            ImageIoDecoder decoder,
            ImageRenderer renderer,
            ImageFileWriter fileWriter
    ) {
        this.input = Objects.requireNonNull(input);
        this.output = Objects.requireNonNull(output);
        this.config = Objects.requireNonNull(config);
        this.existingFilePolicy = Objects.requireNonNull(existingFilePolicy);

        this.scaler = new ImageScaler();
        this.validator = new ImageValidator();

        this.decoder = Objects.requireNonNull(decoder);
        this.renderer = Objects.requireNonNull(renderer);
        this.fileWriter = Objects.requireNonNull(fileWriter);
    }


    public ImageProcessor(File input,
                          File output,
                          ImageConfig config,
                          ExistingFilePolicy existingFilePolicy) {

        this(input, output, config, existingFilePolicy, ImageLimits.defaults());
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
            MorphIOException exception = new MorphIOException(
                    "Image processing failed.",
                    e
            );

            failure = exception;
            throw exception;
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
