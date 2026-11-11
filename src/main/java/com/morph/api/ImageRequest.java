package com.morph.api;

import com.morph.config.ImageConfig;
import com.morph.io.ExistingFilePolicy;

import java.nio.file.Path;
import java.util.Objects;

public final class ImageRequest {

    private final Path input;
    private final Path output;
    private final ImageConfig config;
    private final ExistingFilePolicy existingFilePolicy;

    public ImageRequest(Path input,
                        Path output,
                        ImageConfig config,
                        ExistingFilePolicy existingFilePolicy) {

        this.input = Objects.requireNonNull(input);
        this.output = Objects.requireNonNull(output);
        this.config = Objects.requireNonNull(config);
        this.existingFilePolicy = Objects.requireNonNull(existingFilePolicy);
    }

    public Path getInput() {
        return input;
    }

    public Path getOutput() {
        return output;
    }

    public ImageConfig getConfig() {
        return config;
    }

    public ExistingFilePolicy getExistingFilePolicy() {
        return existingFilePolicy;
    }
}