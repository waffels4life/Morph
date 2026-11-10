package com.morph.codec;

import com.morph.format.ImageFormat;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public interface ImageEncoding {

    void encode(BufferedImage image,
                File output,
                ImageFormat format,
                float quality) throws IOException;
}