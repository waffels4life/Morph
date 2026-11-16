package com.morph.api;

import com.morph.codec.CodecProviderLoader;
import com.morph.codec.decoder.CodecRegistry;
import com.morph.codec.decoder.DecoderContext;
import com.morph.codec.decoder.ImageDecoder;
import com.morph.codec.detection.ImageFormatDetector;
import com.morph.codec.detection.ImageIoFormatDetector;
import com.morph.codec.encoder.EncoderRegistry;
import com.morph.config.ImageLimits;
import com.morph.format.ImageFormat;
import com.morph.io.AtomicMovePolicy;
import com.morph.io.ImageFileWriter;
import com.morph.io.NioFileMover;
import com.morph.process.ImageProcessor;
import com.morph.process.ImageRenderer;
import com.morph.validation.ImageDimensionValidator;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

public final class MorphEngine {

    public Path process(ImageRequest request) throws IOException {

        Objects.requireNonNull(request, "request");

        ImageLimits limits = ImageLimits.defaults();

        ImageDimensionValidator dimensionValidator =
                new ImageDimensionValidator(limits);

        DecoderContext decoderContext =
                new DecoderContext(dimensionValidator);

        CodecRegistry decoderRegistry = new CodecRegistry(
                CodecProviderLoader.loadDecoderProviders(),
                decoderContext
        );

        EncoderRegistry encoderRegistry = new EncoderRegistry(
                CodecProviderLoader.loadEncoderProviders()
        );

        ImageFormatDetector detector = new ImageIoFormatDetector();

        ImageFormat inputFormat = detector.detect(
                request.getInput().toFile()
        );

        ImageDecoder decoder = decoderRegistry.getDecoder(inputFormat);

        ImageProcessor processor = createImageProcessor(request, encoderRegistry, decoder);

        processor.process();

        return request.getOutput();
    }

    private static ImageProcessor createImageProcessor(ImageRequest request, EncoderRegistry encoderRegistry, ImageDecoder decoder) {
        ImageFileWriter fileWriter = new ImageFileWriter(
                encoderRegistry,
                AtomicMovePolicy.REQUIRE_ATOMIC,
                new NioFileMover()
        );

        return new ImageProcessor(
                request.getInput().toFile(),
                request.getOutput().toFile(),
                request.getConfig(),
                request.getExistingFilePolicy(),
                decoder,
                new ImageRenderer(),
                fileWriter
        );
    }
}
