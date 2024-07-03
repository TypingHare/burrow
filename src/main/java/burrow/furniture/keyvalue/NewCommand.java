package burrow.furniture.keyvalue;

import burrow.core.command.Command;
import burrow.core.command.CommandContext;
import burrow.core.command.CommandType;
import burrow.furniture.entry.EntryFurniture;
import org.springframework.lang.NonNull;
import picocli.CommandLine;

@CommandLine.Command(name = "new", description = "Create an entry with key and value.")
@CommandType(EntryFurniture.COMMAND_TYPE)
public class NewCommand extends Command {
    @CommandLine.Parameters(index = "0", description = "The key of the entry.")
    private String key;

    @CommandLine.Parameters(index = "1", description = "The value of the entry.")
    private String value;

    public NewCommand(@NonNull final CommandContext commandContext) {
        super(commandContext);
    }

    @Override
    public Integer call() {
        final var keyValueFurniture = useFurniture(KeyValueFurniture.class);
        final var entry = keyValueFurniture.createEntryWithKeyValue(key, value);
        EntryFurniture.entryToString(context, entry);
        buffer.append(EntryFurniture.entryToString(context, entry));

        return CommandLine.ExitCode.OK;
    }
}