package test.com.morph.process;

import com.morph.config.InterpolationMode;
import com.morph.geometry.ImageDimensions;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.Objects;

public final class ImageRenderer {
    public BufferedImage resize(BufferedImage source,
                                ImageDimensions dimensions,
                                InterpolationMode interpolationMode) {

        Objects.requireNonNull(source, "Source image must not be null.");
        Objects.requireNonNull(dimensions, "Dimensions must not be null.");
        Objects.requireNonNull(interpolationMode, "Interpolation mode must not be null.");

        BufferedImage result = new BufferedImage(
                dimensions.getWidth(),
                dimensions.getHeight(),
                source.getColorModel().hasAlpha()
                        ? BufferedImage.TYPE_INT_ARGB
                        : BufferedImage.TYPE_INT_RGB
        );

        Graphics2D graphics = result.createGraphics();

        try {
            graphics.setRenderingHint(
                    RenderingHints.KEY_INTERPOLATION,
                    switch (interpolationMode) {
                        case NEAREST_NEIGHBOR -> RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
                        case BILINEAR -> RenderingHints.VALUE_INTERPOLATION_BILINEAR;
                        case BICUBIC -> RenderingHints.VALUE_INTERPOLATION_BICUBIC;
                    }
            );

            graphics.drawImage(
                    source,
                    0,
                    0,
                    dimensions.getWidth(),
                    dimensions.getHeight(),
                    null
            );
        }
        finally {
            graphics.dispose();
        }

        return result;
    }
}
