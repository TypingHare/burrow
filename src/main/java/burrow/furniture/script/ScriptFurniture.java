package burrow.furniture.script;

import burrow.core.chamber.Chamber;
import burrow.core.config.Config;
import burrow.core.furniture.BurrowFurniture;
import burrow.core.furniture.Furniture;
import burrow.furniture.exec.ExecFurniture;
import burrow.furniture.hoard.Entry;
import burrow.furniture.pair.PairFurniture;
import org.springframework.lang.NonNull;

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
        config.set(PairFurniture.ConfigKey.PAIR_KEY_NAME, EntryKey.LABEL);
        config.set(PairFurniture.ConfigKey.PAIR_VALUE_NAME, EntryKey.COMMAND);
    }

    @NonNull
    public Entry createEntry(
        @NonNull final String label,
        @NonNull final String command
    ) {
        return use(PairFurniture.class).createEntryWithKeyValue(label, command);
    }

    public void setWorkingDirectory(
        @NonNull final Entry entry,
        @NonNull final String workingDirectory
    ) {
        entry.set(EntryKey.WORKING_DIRECTORY, workingDirectory);
    }

    public @interface EntryKey {
        String WORKING_DIRECTORY = "working_directory";
        String LABEL = "label";
        String COMMAND = "command";
    }
}
