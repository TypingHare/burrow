package burrow.furniture.standard;

import burrow.core.command.Command;
import burrow.core.command.CommandContext;
import burrow.core.command.CommandType;
import org.springframework.lang.NonNull;
import picocli.CommandLine;

@CommandLine.Command(name = "help", description = "Show the usage of a command.")
@CommandType(StandardFurniture.COMMAND_TYPE)
public class HelpCommand extends Command {
    @CommandLine.Parameters(
        index = "0",
        description = "The name of the command.",
        defaultValue = "help")
    private String commandName;

    public HelpCommand(@NonNull final CommandContext context) {
        super(context);
    }

    @Override
    public Integer call() {
        final var command = context.getProcessor().getCommand(commandName);
        if (command == null) {
            buffer.append("No such command: ").append(commandName);
        } else {
            buffer.append(new CommandLine(command).getUsageMessage());
        }

        return CommandLine.ExitCode.OK;
    }
}