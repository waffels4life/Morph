package com.morph.codec;

import com.morph.codec.encoder.ImageEncoder;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public final class FailingImageEncoder implements ImageEncoder {

    @Override
    public void encode(BufferedImage image,
                       File output,
                       float quality) throws IOException {

        throw new IOException("Simulated encoding failure.");
    }
}
