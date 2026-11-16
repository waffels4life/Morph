
package com.morph.codec.decoder;

import com.morph.format.ImageFormat;

import java.util.Set;

public final class ImageIoDecoderProvider implements ImageDecoderProvider {

    private static final Set<ImageFormat> SUPPORTED_FORMATS = Set.of(
            ImageFormat.JPEG,
            ImageFormat.PNG,
            ImageFormat.BMP
    );

    @Override public Set<ImageFormat> getSupportedFormats() {
        return SUPPORTED_FORMATS;
    }

    @Override public ImageDecoder createDecoder(DecoderContext context) {
        return new ImageIoDecoder(context.getDimensionValidator());
    }
}
