package com.morph.io;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

final class UnsupportedAtomicFileMover implements FileMover {

    private boolean atomicMoveCalled;
    private boolean normalMoveCalled;

    @Override
    public void moveAtomically(Path source, Path target) throws IOException {

        atomicMoveCalled = true;

        throw new AtomicMoveNotSupportedException(
                source.toString(),
                target.toString(),
                "Simulated unsupported atomic move."
        );
    }

    @Override
    public void moveNormally(Path source, Path target)
            throws IOException {

        normalMoveCalled = true;

        Files.move(
                source,
                target,
                StandardCopyOption.REPLACE_EXISTING
        );
    }

    boolean wasAtomicMoveCalled() {
        return atomicMoveCalled;
    }

    boolean wasNormalMoveCalled() {
        return normalMoveCalled;
    }
}