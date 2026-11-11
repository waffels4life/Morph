package com.morph.format;

import java.util.Locale;
import java.util.Objects;

public final class ImageFormat {
    public static final ImageFormat JPEG = new ImageFormat("jpeg");
    public static final ImageFormat PNG = new ImageFormat("png");
    public static final ImageFormat BMP = new ImageFormat("bmp");

    private final String id;

    public ImageFormat(String id) {
        Objects.requireNonNull(id);

        String normalize = id.trim().toLowerCase(Locale.ROOT);

        if (normalize.isBlank())
            throw new IllegalArgumentException(
                    "Format id cannot be empty"
            );

        this.id = id;
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
