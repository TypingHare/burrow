package burrow.furniture.script;

import burrow.core.chamber.Chamber;
import burrow.core.config.Config;
import burrow.core.entry.Entry;
import burrow.core.furniture.BurrowFurniture;
import burrow.core.furniture.Furniture;
import burrow.furniture.exec.ExecFurniture;
import burrow.furniture.keyvalue.KeyValueFurniture;
import org.springframework.lang.NonNull;

@BurrowFurniture(
    simpleName = "script",
    description = "Manage and execute scripts.",
    dependencies = {
        KeyValueFurniture.class,
        ExecFurniture.class
    }
)
public class ScriptFurniture extends Furniture {
    public ScriptFurniture(@NonNull final Chamber chamber) {
        super(chamber);
    }

    @Override
    public void init() {
        registerCommand(NewCommand.class);
        registerCommand(ExecCommand.class);
    }

    @NonNull
    public Entry createEntry(
        @NonNull final String label,
        @NonNull final String command
    ) {
        return use(KeyValueFurniture.class).createEntryWithKeyValue(label, command);
    }

    public void setWorkingDirectory(
        @NonNull final Entry entry,
        @NonNull final String workingDirectory
    ) {
        entry.set(EntryKey.WORKING_DIRECTORY, workingDirectory);
    }

    @Override
    public void initConfig(@NonNull final Config config) {
        config.set(KeyValueFurniture.ConfigKey.KV_KEY_NAME, EntryKey.LABEL);
        config.set(KeyValueFurniture.ConfigKey.KV_VALUE_NAME, EntryKey.COMMAND);
        config.set(KeyValueFurniture.ConfigKey.KV_ALLOW_DUPLICATE, false);
    }

    public @interface EntryKey {
        String LABEL = "label";
        String COMMAND = "command";
        String WORKING_DIRECTORY = "working_directory";
    }
}
