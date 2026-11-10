package com.morph.codec;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public interface ImageDecoder {

    BufferedImage decode(File input) throws IOException;
}
