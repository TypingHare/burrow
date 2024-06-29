package burrow.furniture.entry;

import burrow.core.command.Command;
import burrow.core.command.CommandContext;
import burrow.core.command.CommandType;
import org.springframework.lang.NonNull;
import picocli.CommandLine;

import java.util.HashMap;

@CommandLine.Command(name = "new", description = "Create a new entry.")
@CommandType(EntryFurniture.COMMAND_TYPE)
public class NewCommand extends Command {
    @CommandLine.Parameters(arity = "0..*")
    private String[] params;

    public NewCommand(@NonNull final CommandContext commandContext) {
        super(commandContext);
    }

    @Override
    public Integer call() {
        if (params != null && params.length % 2 == 1) {
            buffer.append("Invalid number of arguments: ").append(params.length);
            return CommandLine.ExitCode.USAGE;
        }

        final var properties = new HashMap<String, String>();
        if (params != null) {
            for (int i = 0; i < params.length / 2; ++i) {
                properties.put(params[i * 2], params[i * 2 + 1]);
            }
        }

        final var entry = context.getHoard().create(properties);
        buffer.append(EntryFurniture.entryToString(context, entry));

        return CommandLine.ExitCode.OK;
    }
}