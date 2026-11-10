package com.morph.process;

import com.morph.codec.ImageDecoder;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public final class FakeImageDecoder implements ImageDecoder {

    private final BufferedImage image;

    public FakeImageDecoder(BufferedImage image) {
        this.image = image;
    }

    @Override
    public BufferedImage decode(File input) throws IOException {
        return image;
    }
}
