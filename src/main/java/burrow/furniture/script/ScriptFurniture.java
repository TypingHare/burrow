package burrow.furniture.script;

import burrow.chain.Chain;
import burrow.core.chamber.Chamber;
import burrow.core.config.Config;
import burrow.core.furniture.BurrowFurniture;
import burrow.core.furniture.Furniture;
import burrow.furniture.exec.ExecFurniture;
import burrow.furniture.hoard.Entry;
import burrow.furniture.hoard.HoardFurniture;
import burrow.furniture.hoard.chain.ToFormattedObjectContext;
import burrow.furniture.pair.PairFurniture;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

@BurrowFurniture(
    simpleName = "Script",
    description = "Manage and execute scripts.",
    dependencies = {
        PairFurniture.class,
        ExecFurniture.class
    }
)
public class ScriptFurniture extends Furniture {
    public ScriptFurniture(@NonNull final Chamber chamber) {
        super(chamber);
    }

    @Override
    public void beforeInitialization() {
        registerCommand(NewCommand.class);
        registerCommand(ExecCommand.class);
    }

    @Override
    public void initializeConfig(@NonNull final Config config) {
        config.set(PairFurniture.ConfigKey.PAIR_ALLOW_DUPLICATE, false);
    }

    @Override
    public void initialize() {
        final var hoard = use(HoardFurniture.class);
        hoard.getHoard().getToFormattedObjectChain().use(this::toFormattedObject);
    }

    @NonNull
    public Entry createEntry(
        @NonNull final String label,
        @NonNull final String command
    ) {
        return use(PairFurniture.class).createEntryWithKeyValue(label, command);
    }

    public void toFormattedObject(
        @NonNull final ToFormattedObjectContext context,
        @Nullable final Runnable next
    ) {
        final var formattedObject =
            ToFormattedObjectContext.Hook.formattedObject.getNonNull(context);

        final var key = formattedObject.get(PairFurniture.EntryKey.KEY);
        formattedObject.remove(PairFurniture.EntryKey.KEY);
        formattedObject.put(FormattedObjectKey.LABEL, key);

        final var value = formattedObject.get(PairFurniture.EntryKey.VALUE);
        formattedObject.remove(PairFurniture.EntryKey.VALUE);
        formattedObject.put(FormattedObjectKey.COMMAND, value);

        Chain.runIfNotNull(next);
    }

    public void setWorkingDirectory(
        @NonNull final Entry entry,
        @NonNull final String workingDirectory
    ) {
        entry.set(EntryKey.WORKING_DIRECTORY, workingDirectory);
    }

    public @interface EntryKey {
        String WORKING_DIRECTORY = "working_directory";
    }

    public @interface FormattedObjectKey {
        String LABEL = "label";
        String COMMAND = "command";
    }
}
