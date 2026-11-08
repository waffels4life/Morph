package com.morph.format;

public enum ImageFormat {
    JPEG("jpeg", "jpg"),
    PNG("png", "png"),
    BMP("bmp", "bmp");

    private final String formatName;
    private final String extension;

    ImageFormat(String formatName, String extension) {
        this.formatName = formatName;
        this.extension = extension;
    }

    public String getFormatName() {
        return formatName;
    }

    public String getExtension() {
        return extension;
    }
}
