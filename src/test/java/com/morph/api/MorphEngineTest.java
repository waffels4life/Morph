package com.morph.api;

import com.morph.config.ImageConfig;
import com.morph.config.InterpolationMode;
import com.morph.format.ImageFormat;
import com.morph.io.ExistingFilePolicy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class MorphEngineTest {

    @TempDir
    Path tempDir;


    @Test
    void shouldProcessImageSuccessfully() throws IOException {

        Path input = tempDir.resolve("input.png");
        Path output = tempDir.resolve("output.png");

        BufferedImage source = new BufferedImage(
                800,
                600,
                BufferedImage.TYPE_INT_RGB
        );

        ImageIO.write(source, "png", input.toFile());

        ImageConfig config = new ImageConfig(
                400,
                400,
                ImageFormat.PNG,
                1.0f,
                InterpolationMode.BICUBIC
        );

        ImageRequest request = new ImageRequest(
                input,
                output,
                config,
                ExistingFilePolicy.FAIL_IF_EXISTS
        );

        MorphEngine engine = new MorphEngine();

        Path result = engine.process(request);

        assertNotNull(result);
        assertEquals(output, result);
        assertTrue(Files.exists(output));

        BufferedImage processed = ImageIO.read(output.toFile());

        assertNotNull(processed);
        assertEquals(400, processed.getWidth());
        assertEquals(300, processed.getHeight());

        processed.flush();
        source.flush();
    }

}
