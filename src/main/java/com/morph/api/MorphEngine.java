package com.morph.api;

import com.morph.process.ImageProcessor;

import java.io.File;
import java.util.Objects;

public final class MorphEngine {

    public File process(ImageRequest request) {

        Objects.requireNonNull(request, "request");

        ImageProcessor processor = new ImageProcessor(
                request.getInput(),
                request.getOutput(),
                request.getConfig(),
                request.getExistingFilePolicy()
        );

        return processor.process();
    }
}
