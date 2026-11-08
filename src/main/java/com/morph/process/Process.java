package com.morph.process;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class Process {

    private File input;
    private File output = new File("output.png");
    private BufferedImage image;
    private Graphics2D graphics2D;

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

    public void drawImageGraphic(BufferedImage resizeImage) {
        Objects.requireNonNull(resizeImage);
        this.graphics2D = resizeImage.createGraphics();

        try {
            this.graphics2D.setRenderingHint(
                    RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BICUBIC
            );

            this.graphics2D.drawImage(
                    image, 0, 0, 800, 600, null
            );
        }
        finally {
            this.graphics2D.dispose();
        }
    }

    public File getOutput() {
        return output;
    }

    public BufferedImage getImage() {
        return image;
    }
}
