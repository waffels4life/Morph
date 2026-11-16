package com.morph.codec;

import com.morph.codec.decoder.ImageDecoderProvider;
import com.morph.codec.encoder.ImageEncoderProvider;

import java.util.HashSet;
import java.util.ServiceLoader;
import java.util.Set;

public final class CodecProviderLoader {

    private CodecProviderLoader() {}

    public static Set<ImageDecoderProvider> loadDecoderProviders() {
        Set<ImageDecoderProvider> providers = new HashSet<>();

        ServiceLoader.load(ImageDecoderProvider.class)
                .forEach(providers::add);

        return Set.copyOf(providers);
    }

    public static Set<ImageEncoderProvider> loadEncoderProviders() {
        Set<ImageEncoderProvider> providers = new HashSet<>();

        ServiceLoader.load(ImageEncoderProvider.class)
                .forEach(providers::add);

        return Set.copyOf(providers);
    }
}
