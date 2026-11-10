package com.morph.io;

import java.io.IOException;
import java.nio.file.Path;

public interface FileMover {

    void moveAtomically(Path source, Path target) throws IOException;

    void moveNormally(Path source, Path target) throws IOException;
}
