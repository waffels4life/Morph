package com.morph;

import com.morph.config.ImageConfig;
import com.morph.format.ImageFormat;
import com.morph.process.ImageProcessor;

import java.io.File;

public class Morph {
    public static void main(String[] args) {
        new ImageProcessor(
                new File("in.png"),
                new File("out.png"),
                new ImageConfig(600,
                        800,
                        ImageFormat.JPEG,
                        0.85f)).process();
    }
}