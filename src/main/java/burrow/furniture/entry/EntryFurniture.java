package burrow.furniture.entry;

import burrow.core.chamber.Chamber;
import burrow.core.furniture.Furniture;
import org.springframework.lang.NonNull;

public class EntryFurniture extends Furniture {
    public static final String COMMAND_TYPE = "Entry";

    public EntryFurniture(@NonNull final Chamber chamber) {
        super(chamber);
    }
}
