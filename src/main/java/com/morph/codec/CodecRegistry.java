package com.morph.codec;

import com.morph.format.ImageFormat;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class CodecRegistry {

    private final Map<ImageFormat, ImageDecoder> decoders;

    public CodecRegistry(Set<ImageDecoderProvider> providers, DecoderContext context) {
        Objects.requireNonNull(providers, "Providers cannot be null.");
        Objects.requireNonNull(context, "Decoder context cannot be null.");

        Map<ImageFormat, ImageDecoder> registered = new HashMap<>();

        for (ImageDecoderProvider provider : providers) {
            Objects.requireNonNull(provider, "Provider cannot be null.");

            Set<ImageFormat> formats = Objects.requireNonNull(
                    provider.getSupportedFormats(),
                    "Supported formats cannot be null."
            );

            ImageDecoder decoder = Objects.requireNonNull(
                    provider.createDecoder(context),
                    "Provider returned a null decoder."
            );

            for (ImageFormat format : formats) {
                Objects.requireNonNull(format, "Supported format cannot be null.");

                ImageDecoder previous = registered.putIfAbsent(format, decoder);

                if (previous != null)
                    throw new IllegalStateException(
                            "Multiple decoder providers support format: " + format
                    );
            }
        }

        this.decoders = Map.copyOf(registered);
    }

    public ImageDecoder getDecoder(ImageFormat format) {
        Objects.requireNonNull(format, "Image format cannot be null.");

        ImageDecoder decoder = decoders.get(format);

        if (decoder == null)
            throw new IllegalArgumentException(
                    "No decoder registered for format: " + format
            );

        return decoder;
    }
}
