package burrow.furniture.hoard.exception;

import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;

public final class HoardFileNotFoundException extends FileNotFoundException {
    public HoardFileNotFoundException(@NotNull final String hoardFilePath) {
        super("Hoard file not found: " + hoardFilePath);
    }
}
