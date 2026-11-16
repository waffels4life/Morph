package com.morph.api;

import com.morph.codec.CodecProviderLoader;
import com.morph.codec.decoder.CodecRegistry;
import com.morph.codec.decoder.DecoderContext;
import com.morph.codec.decoder.ImageDecoder;
import com.morph.codec.detection.ImageFormatDetector;
import com.morph.codec.detection.ImageIoFormatDetector;
import com.morph.config.ImageLimits;
import com.morph.format.ImageFormat;
import com.morph.io.ImageFileWriter;
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
        ImageDimensionValidator dimensionValidator = new ImageDimensionValidator(limits);
        DecoderContext context = new DecoderContext(dimensionValidator);
        CodecRegistry registry = new CodecRegistry(
                CodecProviderLoader.loadDecoderProviders(),
                context
        );

        ImageFormatDetector detector = new ImageIoFormatDetector();

        ImageFormat inputFormat = detector.detect(
                request.getInput().toFile()
        );

        ImageDecoder decoder = registry.getDecoder(inputFormat);


        ImageProcessor processor = new ImageProcessor(
                request.getInput().toFile(),
                request.getOutput().toFile(),
                request.getConfig(),
                request.getExistingFilePolicy(),
                decoder,
                new ImageRenderer(),
                new ImageFileWriter()
        );

        processor.process();

        return request.getOutput();
    }
}
