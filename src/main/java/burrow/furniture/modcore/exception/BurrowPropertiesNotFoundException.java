package burrow.furniture.modcore.exception;

import burrow.furniture.modcore.ModCoreFurniture;
import org.springframework.lang.NonNull;

import java.nio.file.Path;

public final class BurrowPropertiesNotFoundException extends Exception {
    public BurrowPropertiesNotFoundException(@NonNull final Path jarPath) {
        super(String.format("Mod JAR %s does not contain %s", jarPath.toAbsolutePath(), ModCoreFurniture.BURROW_JAR_PROPERTIES_FILE));
    }
}
