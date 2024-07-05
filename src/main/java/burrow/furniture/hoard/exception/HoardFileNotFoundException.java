package burrow.furniture.hoard.exception;

import org.springframework.lang.NonNull;

import java.io.FileNotFoundException;

public final class HoardFileNotFoundException extends FileNotFoundException {
    public HoardFileNotFoundException(@NonNull final String hoardFilePath) {
        super("Hoard file not found: " + hoardFilePath);
    }
}
