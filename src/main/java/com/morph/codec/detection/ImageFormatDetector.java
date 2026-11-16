package com.morph.codec.detection;

import com.morph.format.ImageFormat;

import java.io.File;
import java.io.IOException;

public interface ImageFormatDetector {

    ImageFormat detect(File input) throws IOException;
}
