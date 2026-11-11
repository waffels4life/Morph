package com.morph.api;

import com.morph.codec.ImageDecoder;
import com.morph.codec.ImageIoDecoder;
import com.morph.config.ImageLimits;
import com.morph.io.ImageFileWriter;
import com.morph.process.ImageProcessor;
import com.morph.process.ImageRenderer;
import com.morph.validation.ImageValidator;

import java.nio.file.Path;
import java.util.Objects;

public final class MorphEngine {

    public Path process(ImageRequest request) {

        Objects.requireNonNull(request, "request");

        ImageLimits limits = ImageLimits.defaults();
        ImageValidator validator = new ImageValidator();

        ImageDecoder decoder = new ImageIoDecoder(limits, validator);

        ImageProcessor processor = new ImageProcessor(
                request.getInput().toFile(),
                request.getOutput().toFile(),
                request.getConfig(),
                request.getExistingFilePolicy(),
                decoder,
                new ImageRenderer(),
                new ImageFileWriter()
        );

        processor.process();

        return request.getOutput();
    }
}
