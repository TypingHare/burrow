package burrow.furniture.keyvalue;

import burrow.core.command.Command;
import burrow.core.command.CommandContext;
import burrow.core.command.CommandType;
import burrow.furniture.standard.StandardFurniture;
import org.springframework.lang.NonNull;
import picocli.CommandLine;

@CommandLine.Command(name = "values", description = "Retrieve values of a specified key.")
@CommandType(KeyValueFurniture.COMMAND_TYPE)
public class ValuesCommand extends Command {
    @CommandLine.Parameters(index = "0", description = "The key of the entry.")
    private String key;

    public ValuesCommand(@NonNull final CommandContext commandContext) {
        super(commandContext);
    }

    @Override
    public Integer call() {
        final var valueList = KeyValueFurniture.getValueListByKey(context, key);
        buffer.append(StandardFurniture.stringListToString(commandContext, valueList));

        return CommandLine.ExitCode.OK;
    }
}
