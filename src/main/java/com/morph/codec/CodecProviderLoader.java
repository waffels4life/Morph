package com.morph.codec;

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
}
