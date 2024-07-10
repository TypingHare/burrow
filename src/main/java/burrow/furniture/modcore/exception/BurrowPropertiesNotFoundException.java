package burrow.furniture.modcore.exception;

import burrow.furniture.modcore.ModCoreFurniture;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public final class BurrowPropertiesNotFoundException extends Exception {
    public BurrowPropertiesNotFoundException(@NotNull final Path jarPath) {
        super(String.format("Mod JAR %s does not contain %s", jarPath.toAbsolutePath(), ModCoreFurniture.BURROW_JAR_PROPERTIES_FILE));
    }
}
