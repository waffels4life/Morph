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

    public void imageReader() {
        if (input == null)
            throw new RuntimeException();

        try {
            this.image = ImageIO.read(input);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public BufferedImage resizeImage(BufferedImage image) {
        return new BufferedImage(
                600,
                800,
                BufferedImage.TYPE_INT_RGB
        );
    }

    public File getOutput() {
        return output;
    }

    public BufferedImage getImage() {
        return image;
    }
}
