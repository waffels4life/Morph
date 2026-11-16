package com.morph.codec.encoder;

import com.morph.format.ImageFormat;

import java.util.Objects;
import java.util.Set;

public final class ImageIoEncoderProvider implements ImageEncoderProvider {

    private static final Set<ImageFormat> SUPPORTED_FORMATS = Set.of(
            ImageFormat.JPEG,
            ImageFormat.PNG,
            ImageFormat.BMP
    );

    @Override public Set<ImageFormat> getSupportedFormats() {
        return SUPPORTED_FORMATS;
    }

    @Override
    public ImageEncoder createEncoder(ImageFormat format) {
        Objects.requireNonNull(format, "Format cannot be null.");

        if (!SUPPORTED_FORMATS.contains(format))
            throw new IllegalArgumentException(
                    "Unsupported image format: " + format.getId()
            );

        return new ImageIoEncoder(format);
    }
}
