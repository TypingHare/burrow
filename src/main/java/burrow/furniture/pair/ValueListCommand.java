package burrow.furniture.pair;

import burrow.core.command.Command;
import burrow.core.command.CommandContext;
import burrow.core.command.CommandType;
import burrow.furniture.standard.StandardFurniture;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

@CommandLine.Command(name = "value.list", description = "Retrieve values of a specified key.")
@CommandType(PairFurniture.COMMAND_TYPE)
public class ValueListCommand extends Command {
    @CommandLine.Parameters(index = "0", description = "The key of the entry.")
    private String key;

    public ValueListCommand(@NotNull final CommandContext commandContext) {
        super(commandContext);
    }

    @Override
    public Integer call() {
        final var valueList = use(PairFurniture.class).getValueListByKey(key);
        buffer.append(StandardFurniture.stringListToString(commandContext, valueList));

        return CommandLine.ExitCode.OK;
    }
}
