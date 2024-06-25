package burrow.core.entry;

import java.io.FileNotFoundException;

public class HoardFileNotFoundException extends FileNotFoundException {
    public HoardFileNotFoundException(final String hoardFilePath) {
        super("Hoard file not found: " + hoardFilePath);
    }
}
