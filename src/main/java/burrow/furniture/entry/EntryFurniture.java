package burrow.furniture.entry;

import burrow.core.chain.FormatEntryChain;
import burrow.core.chamber.Chamber;
import burrow.core.chamber.ChamberContext;
import burrow.core.entry.Entry;
import burrow.core.furniture.BurrowFurniture;
import burrow.core.furniture.Furniture;
import org.springframework.lang.NonNull;

import java.util.HashMap;

@BurrowFurniture(
    simpleName = "entry",
    description = "entry"
)
public class EntryFurniture extends Furniture {
    public static final String COMMAND_TYPE = "Entry";

    public EntryFurniture(@NonNull final Chamber chamber) {
        super(chamber);

        registerCommand(EntryCommand.class);
        registerCommand(NewCommand.class);
        registerCommand(DeleteCommand.class);
        registerCommand(ExistCommand.class);
        registerCommand(CountCommand.class);
        registerCommand(EntriesCommand.class);
        registerCommand(PropCommand.class);
        registerCommand(SetCommand.class);
        registerCommand(UnsetCommand.class);
    }

    public static int getEntryCount(@NonNull final ChamberContext chamberContext) {
        return chamberContext.getHoard().getAllEntries().size();
    }

    @NonNull
    public static String entryToString(
        @NonNull final ChamberContext chamberContext,
        @NonNull final Entry entry
    ) {
        final var entryObject = new HashMap<String, String>();
        final var toFormattedObjectChain = chamberContext.getOverseer().getToFormattedObjectChain();
        final var toFormattedObjectContext =
            toFormattedObjectChain.createContext(entry, entryObject);
        toFormattedObjectChain.apply(toFormattedObjectContext);

        final var formatEntryChain = chamberContext.getOverseer().getFormatEntryChain();
        final var formatEntryContext = formatEntryChain.createContext(entry.getId(), entryObject);
        formatEntryChain.apply(formatEntryContext);

        return FormatEntryChain.resultHook.get(formatEntryContext);
    }
}
