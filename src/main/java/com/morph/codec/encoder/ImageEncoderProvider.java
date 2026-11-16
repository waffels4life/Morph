package com.morph.codec.encoder;

import com.morph.format.ImageFormat;

import java.util.Set;

public interface ImageEncoderProvider {

    Set<ImageFormat> getSupportedFormats();

    ImageEncoder createEncoder(ImageFormat format);
}
