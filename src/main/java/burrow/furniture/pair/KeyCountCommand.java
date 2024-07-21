package burrow.furniture.pair;

import burrow.core.command.Command;
import burrow.core.command.CommandContext;
import burrow.core.command.CommandType;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

@CommandLine.Command(
    name = "key.count",
    description = "Get the count of entries associated with a specified key."
)
@CommandType(PairFurniture.COMMAND_TYPE)
public class KeyCountCommand extends Command {
    @CommandLine.Parameters(index = "0", description = "The key.")
    private String key;

    public KeyCountCommand(@NotNull final CommandContext commandContext) {
        super(commandContext);
    }

    @Override
    public Integer call() {
        final var pairFurniture = use(PairFurniture.class);
        buffer.append(pairFurniture.countByKey(key));

        return CommandLine.ExitCode.OK;
    }
}
