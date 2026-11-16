package com.morph.codec.encoder;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public interface ImageEncoder {

    void encode(
            BufferedImage image,
            File output,
            float quality
    ) throws IOException;
}
