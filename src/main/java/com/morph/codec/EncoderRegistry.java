package com.morph.codec;

import com.morph.format.ImageFormat;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class EncoderRegistry {

    private final Map<ImageFormat, ImageEncoder> encoders;

    public EncoderRegistry(Set<ImageEncoderProvider> providers) {
        Objects.requireNonNull(providers, "Providers cannot be null.");

        Map<ImageFormat, ImageEncoder> registered = new HashMap<>();

        for (ImageEncoderProvider provider : providers) {
            Objects.requireNonNull(provider, "Provider cannot be null.");

            Set<ImageFormat> formats = Objects.requireNonNull(
                    provider.getSupportedFormats(),
                    "Supported formats cannot be null."
            );

            for (ImageFormat format : formats) {
                Objects.requireNonNull(format, "Format cannot be null.");

                ImageEncoder encoder = Objects.requireNonNull(
                        provider.createEncoder(format),
                        "Provider returned a null encoder."
                );

                if (registered.putIfAbsent(format, encoder) != null)
                    throw new IllegalStateException(
                            "Encoder already registered for format: "
                                    + format.getId()
                    );
            }
        }

        this.encoders = Map.copyOf(registered);
    }

    public ImageEncoder getEncoder(ImageFormat format) {
        Objects.requireNonNull(format, "Format cannot be null.");

        ImageEncoder encoder = encoders.get(format);

        if (encoder == null)
            throw new IllegalArgumentException(
                    "No encoder registered for format: " + format.getId()
            );

        return encoder;
    }
}
