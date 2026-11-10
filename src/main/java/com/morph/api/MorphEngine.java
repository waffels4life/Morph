package com.morph.api;

import com.morph.codec.ImageDecoder;
import com.morph.config.ImageLimits;
import com.morph.io.ImageFileWriter;
import com.morph.process.ImageProcessor;
import com.morph.process.ImageRenderer;
import com.morph.validation.ImageValidator;

import java.io.File;
import java.util.Objects;

public final class MorphEngine {

    public File process(ImageRequest request) {

        Objects.requireNonNull(request, "request");

        ImageLimits limits = ImageLimits.defaults();
        ImageValidator validator = new ImageValidator();

        ImageDecoder decoder = new ImageDecoder(limits, validator);

        ImageProcessor processor = new ImageProcessor(
                request.getInput(),
                request.getOutput(),
                request.getConfig(),
                request.getExistingFilePolicy(),
                decoder,
                new ImageRenderer(),
                new ImageFileWriter()
        );

        return processor.process();
    }
}
