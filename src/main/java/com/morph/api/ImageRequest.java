package com.morph.api;

import com.morph.config.ImageConfig;
import com.morph.io.ExistingFilePolicy;

import java.io.File;
import java.util.Objects;

public final class ImageRequest {

    private final File input;
    private final File output;
    private final ImageConfig config;
    private final ExistingFilePolicy existingFilePolicy;

    public ImageRequest(File input,
                        File output,
                        ImageConfig config,
                        ExistingFilePolicy existingFilePolicy) {

        this.input = Objects.requireNonNull(input);
        this.output = Objects.requireNonNull(output);
        this.config = Objects.requireNonNull(config);
        this.existingFilePolicy = Objects.requireNonNull(existingFilePolicy);
    }

    public File getInput() {
        return input;
    }

    public File getOutput() {
        return output;
    }

    public ImageConfig getConfig() {
        return config;
    }

    public ExistingFilePolicy getExistingFilePolicy() {
        return existingFilePolicy;
    }
}