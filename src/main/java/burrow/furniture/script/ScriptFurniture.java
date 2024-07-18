package burrow.furniture.script;

import burrow.core.chamber.Chamber;
import burrow.core.config.Config;
import burrow.core.furniture.BurrowFurniture;
import burrow.core.furniture.Furniture;
import burrow.furniture.hoard.Entry;
import burrow.furniture.pair.PairFurniture;
import burrow.furniture.shellcore.ShellCoreFurniture;
import org.jetbrains.annotations.NotNull;

@BurrowFurniture(
    simpleName = "Script",
    description = "Manage and execute scripts.",
    type = BurrowFurniture.Type.MAIN,
    dependencies = {
        PairFurniture.class,
        ShellCoreFurniture.class
    }
)
public class ScriptFurniture extends Furniture {
    public static final String COMMAND_TYPE = "Script";

    public ScriptFurniture(@NotNull final Chamber chamber) {
        super(chamber);
    }

    @Override
    public void beforeInitialization() {
        registerCommand(NewCommand.class);
        registerCommand(ExecCommand.class);
    }

    @Override
    public void initializeConfig(@NotNull final Config config) {
        config.set(PairFurniture.ConfigKey.PAIR_ALLOW_DUPLICATE, false);
        config.set(PairFurniture.ConfigKey.PAIR_KEY_NAME, EntryKey.LABEL);
        config.set(PairFurniture.ConfigKey.PAIR_VALUE_NAME, EntryKey.COMMAND);
    }

    @NotNull
    public Entry createEntry(
        @NotNull final String label,
        @NotNull final String command
    ) {
        return use(PairFurniture.class).createEntryWithKeyValue(label, command);
    }

    public void setWorkingDirectory(
        @NotNull final Entry entry,
        @NotNull final String workingDirectory
    ) {
        entry.set(EntryKey.WORKING_DIRECTORY, workingDirectory);
    }

    public @interface EntryKey {
        String LABEL = "label";
        String COMMAND = "command";
        String WORKING_DIRECTORY = "working_directory";
        String IS_POST = "is_post";
    }
}
