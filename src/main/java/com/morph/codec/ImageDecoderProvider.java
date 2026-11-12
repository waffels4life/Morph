package com.morph.codec;

import com.morph.format.ImageFormat;

import java.util.Set;

public interface ImageDecoderProvider {

    Set<ImageFormat> getSupportedFormats();

    ImageDecoder createDecoder(DecoderContext decoderContext);
}
