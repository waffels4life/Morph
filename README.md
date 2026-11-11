<p align="center">
  <img src="assets/morph-logo.png" alt="Morph Logo" width="220" style="border-radius: 6px">
</p>

<h1 align="center">Morph</h1>

<p align="center">
  A lightweight image processing engine written in Java.
</p>

## Overview

Morph is a lightweight Java image processing engine designed to provide a simple and predictable way to resize images and control their output format.

It focuses on clear separation of responsibilities, controlled resource management, safe file operations, and explicit error handling.

Morph is designed as an extensible foundation rather than a feature-heavy image editing framework.

## Features

- Image resizing with aspect ratio preservation.
- Configurable interpolation methods:
  - Nearest Neighbor
  - Bilinear
  - Bicubic
- Support for JPEG, PNG, and BMP formats.
- Configurable JPEG compression quality.
- Configurable output dimensions.
- Input and output validation.
- Configurable image dimension and pixel-count limits.
- Explicit output file overwrite policies.
- Temporary-file-based output writing.
- Atomic file movement with a controlled fallback mechanism.
- Dedicated exception hierarchy for predictable error handling.
- Decoder and encoder abstractions for future extensibility.
- Immutable request and configuration objects.
- Path-based public API using Java NIO.

## Architecture

Morph follows a modular, responsibility-oriented design. Each component handles a specific part of the image processing workflow.

The main processing pipeline consists of:

1. **Validation** — checks input files, output destinations, and image dimensions.
2. **Decoding** — reads image data through a decoder abstraction.
3. **Dimension Calculation** — calculates target dimensions while preserving the original aspect ratio.
4. **Rendering** — resizes the image using the configured interpolation method.
5. **Encoding and Writing** — encodes the processed image and safely writes it to the destination.

### Design Principles

- **Separation of Concerns:** Validation, decoding, rendering, and file writing are handled by separate components.
- **Abstraction:** Decoder and encoder implementations are accessed through interfaces, reducing coupling to specific implementations.
- **Immutability:** Public request and configuration objects are immutable after construction.
- **Dependency Injection:** Core processing components support dependency injection, allowing alternative implementations without changing the processing workflow.
- **Composition Root:** `MorphEngine` assembles the required components and exposes a simple public API.

This design keeps the processing workflow independent of specific decoder and encoder implementations, while allowing individual components to evolve separately.

## Reliability & Security

Morph incorporates several defensive measures to make image processing more predictable and reduce common resource-related risks.

### Image Validation

Before decoding an image, Morph validates its dimensions using image metadata.

The default limits are:

| Limit | Default |
|---|---:|
| Maximum width | 10,000 pixels |
| Maximum height | 10,000 pixels |
| Maximum pixel count | 40,000,000 |

These limits help prevent excessively large images from being decoded and consuming unreasonable amounts of memory.

The dimension checks are performed before the actual image decoding operation.

### Resource Management

- Image input streams are closed using try-with-resources.
- Image readers and writers are explicitly disposed of after use.
- Temporary resources are cleaned up after processing.
- Decoded and rendered `BufferedImage` instances are flushed when processing finishes.
- Cleanup failures are preserved without silently replacing the original processing exception.

These measures help reduce resource leaks during normal operation and failure scenarios.

### Defensive Validation

Morph validates:

- Input file existence and readability.
- Output directory existence and writability.
- Input and output path conflicts.
- Existing output file policies.
- Image dimensions and total pixel count.
- Required configuration values.

**Security note:** These measures reduce certain resource-exhaustion and file-handling risks. They do not guarantee protection against every malformed image, decompression bomb, filesystem race condition, or other security threat.

## File Safety

Morph treats output file operations as an important part of the processing workflow.

### Explicit Overwrite Policies

Existing output files are not overwritten by default.

Morph supports two policies:

- `FAIL_IF_EXISTS` — rejects processing when the destination file already exists.
- `REPLACE_EXISTING` — explicitly allows replacing an existing destination file.

The caller must choose the intended behavior through the request configuration.

### Temporary File Writing

Instead of writing directly to the final destination, Morph follows a temporary-file-based workflow:

1. Creates a temporary file in the destination directory.
2. Encodes the processed image into the temporary file.
3. Moves the completed file to its destination.
4. Cleans up the temporary file.

This approach helps prevent incomplete output files from appearing at the final destination when encoding fails.

### Atomic File Movement

Morph attempts to use atomic file movement when replacing an existing destination.

If the filesystem does not support atomic movement, a fallback mechanism can be used according to the configured atomic move policy.

Atomic movement is filesystem-dependent and is not guaranteed in every environment.

## Error Handling

Morph uses a dedicated exception hierarchy to provide a consistent error-handling model.

The exception hierarchy is:

```text
RuntimeException
    └── MorphException
          ├── MorphIOException
          └── InvalidImageException
```

### Exception Behavior

| Exception | Purpose |
|---|---|
| `MorphException` | Base exception for Morph-specific failures. |
| `MorphIOException` | Represents I/O failures during image processing. |
| `InvalidImageException` | Represents invalid, unsupported, or undecodable image data. |
| `IllegalArgumentException` | Represents invalid API arguments or configuration values. |

Checked I/O exceptions are translated into `MorphIOException` at the processing boundary.

Runtime exceptions retain their original types, allowing callers to handle specific failures without losing useful information.

Morph also preserves secondary cleanup failures as suppressed exceptions when another failure has already occurred.

## Requirements

- Java 21 or later.
- A compatible Java runtime environment.

Morph currently uses Java's built-in ImageIO API for image decoding and encoding.

## Usage

The public API is based on `Path`, `ImageRequest`, `ImageConfig`, and `MorphEngine`.

### Basic Example

```java
Path input = Path.of("images/input.png");
Path output = Path.of("images/output.jpg");

ImageConfig config = ImageConfig.builder()
        .width(800)
        .height(600)
        .format(ImageFormat.JPEG)
        .quality(0.85f)
        .interpolationMode(InterpolationMode.BICUBIC)
        .build();

ImageRequest request = ImageRequest.builder()
        .input(input)
        .output(output)
        .config(config)
        .existingFilePolicy(ExistingFilePolicy.FAIL_IF_EXISTS)
        .build();

MorphEngine engine = new MorphEngine();

Path result = engine.process(request);
```

The example resizes the input image while preserving its aspect ratio and writes the result as a JPEG image with the specified compression quality.

The example assumes the input file and output directory already exist.

### Handling Errors

```java
try {
Path result = engine.process(request);
    System.out.println("Image saved to: " + result);
}
catch (InvalidImageException e) {
    System.err.println("Invalid image: " + e.getMessage());
}
catch (MorphIOException e) {
    System.err.println("Image I/O failure: " + e.getMessage());
}
catch (IllegalArgumentException e) {
    System.err.println("Invalid configuration: " + e.getMessage());
}
```

## Supported Formats

Morph currently supports:

| Format | Decoding | Encoding | Configurable Quality |
|---|---|---|---|
| JPEG | Yes | Yes | Yes |
| PNG | Yes | Yes | No |
| BMP | Yes | Yes | No |

Format support is based on the current `ImageFormat` implementation and available Java ImageIO providers.

JPEG compression quality is configurable between `0.0` and `1.0`.

## Testing

Morph includes automated unit tests covering its core components and image processing workflow.

The test suite covers:

- Image decoding and validation.
- Image processing and resizing.
- Output file writing and overwrite policies.
- Resource cleanup and exception handling.
- Public API behavior.

The project uses JUnit 6 for automated testing.

Run the test suite with Maven:

```bash
mvn test
```

**Latest verification:** 28 tests passed, with 0 failures and 0 errors.


## Limitations

The current release intentionally keeps its scope small.

- Only JPEG, PNG, and BMP are exposed through the current format configuration.
- JPEG is the only format with configurable compression quality.
- Default image limits are currently fixed inside `MorphEngine`.
- The public API currently uses filesystem paths rather than stream-based input and output.
- Alternative decoder and encoder implementations can be injected into internal processing components, but a public plugin registration system is not currently provided.
- Advanced image transformations and editing operations are outside the current scope.

## Roadmap

Potential areas of future development include:

- Configurable engine-level options and image limits.
- Additional image formats such as WebP and AVIF.
- Extensible decoder and encoder registration.
- Stream-based input and output support.
- Additional interpolation algorithms.
- More advanced image optimization capabilities.

These are potential future improvements, not features of the current release.

## License

License information has not been specified yet.
