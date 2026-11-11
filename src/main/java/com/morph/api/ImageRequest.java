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

    private ImageRequest(Builder builder) {
        this.input = Objects.requireNonNull(builder.input, "input");
        this.output = Objects.requireNonNull(builder.output, "output");
        this.config = Objects.requireNonNull(builder.config, "config");
        this.existingFilePolicy = Objects.requireNonNull(
                builder.existingFilePolicy,
                "existingFilePolicy"
        );
    }

    public static Builder builder() {
        return new Builder();
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

    public static final class Builder {

        private Path input;
        private Path output;
        private ImageConfig config;
        private ExistingFilePolicy existingFilePolicy =
                ExistingFilePolicy.FAIL_IF_EXISTS;

        private Builder() {
        }

        public Builder input(Path input) {
            this.input = input;
            return this;
        }

        public Builder output(Path output) {
            this.output = output;
            return this;
        }

        public Builder config(ImageConfig config) {
            this.config = config;
            return this;
        }

        public Builder existingFilePolicy(ExistingFilePolicy policy) {
            this.existingFilePolicy = policy;
            return this;
        }

        public ImageRequest build() {
            return new ImageRequest(this);
        }
    }
}
