package burrow.furniture.pair;

import burrow.core.command.Command;
import burrow.core.command.CommandContext;
import burrow.core.command.CommandType;
import burrow.furniture.hoard.HoardFurniture;
import org.springframework.lang.NonNull;
import picocli.CommandLine;

@CommandLine.Command(name = "new", description = "Create an entry with key and value.")
@CommandType(PairFurniture.COMMAND_TYPE)
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
        final var pairFurniture = use(PairFurniture.class);
        final var entry = pairFurniture.createEntryWithKeyValue(key, value);
        final var hoardFurniture = use(HoardFurniture.class);
        buffer.append(hoardFurniture.entryToString(entry, commandContext));

        return CommandLine.ExitCode.OK;
    }
}