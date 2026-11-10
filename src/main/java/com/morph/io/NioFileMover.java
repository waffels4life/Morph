
package com.morph.io;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public final class NioFileMover implements FileMover {

    @Override public void moveAtomically(Path source, Path target) throws IOException {

        Files.move(
                source,
                target,
                StandardCopyOption.ATOMIC_MOVE,
                StandardCopyOption.REPLACE_EXISTING
        );
    }

    @Override public void moveNormally(Path source, Path target) throws IOException {

        Files.move(
                source,
                target,
                StandardCopyOption.REPLACE_EXISTING
        );
    }
}
