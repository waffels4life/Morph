package com.morph.format;

import java.util.Locale;
import java.util.Objects;

public final class ImageFormat {
    public static final ImageFormat JPEG = new ImageFormat("jpeg", "jpg");
    public static final ImageFormat PNG = new ImageFormat("png", "png");
    public static final ImageFormat BMP = new ImageFormat("bmp", "bmp");

    private final String id;
    private final String extension;

    public ImageFormat(String id, String extension) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(extension, "extension");

        this.id = id.trim().toLowerCase(Locale.ROOT);
        this.extension = extension.trim().toLowerCase(Locale.ROOT);

        if (this.id.isEmpty() || this.extension.isEmpty())
            throw new IllegalArgumentException(
                    "Format id and extension cannot be empty."
            );
    }

    public String getId() {
        return id;
    }

    public String getExtension() {
        return extension;
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ImageFormat other)) return false;

        return id.equals(other.id);
    }

    @Override public int hashCode() {
        return id.hashCode();
    }

    @Override public String toString() {
        return id;
    }
}
