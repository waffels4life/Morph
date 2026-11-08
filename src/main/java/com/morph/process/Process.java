package com.morph.process;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Process {

    private File input;
    private File output = new File("output.png");
    private BufferedImage image;

    public void inputFile(String pathname) {
        this.input = new File(pathname);
    }


}
