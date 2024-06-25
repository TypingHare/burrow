package burrow.furniture.standard;

import burrow.core.chamber.Chamber;
import burrow.core.furniture.BurrowFurniture;
import burrow.core.furniture.Furniture;
import org.springframework.lang.NonNull;

@BurrowFurniture(
    simpleName = "standard",
    description = "Standard commands."
)
public class StandardFurniture extends Furniture {
    public static final String COMMAND_TYPE = "standard";

    public StandardFurniture(@NonNull final Chamber chamber) {
        super(chamber);
    }

    @Override
    public void init() {
        registerCommand(RootCommand.class);
    }
}
