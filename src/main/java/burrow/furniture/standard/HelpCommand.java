package burrow.furniture.standard;

import burrow.core.command.Command;
import burrow.core.command.CommandContext;
import burrow.core.command.CommandType;
import org.springframework.lang.NonNull;
import picocli.CommandLine;

@CommandLine.Command(name = "help", description = "Display the usage of a command.")
@CommandType(StandardFurniture.COMMAND_TYPE)
public class HelpCommand extends Command {
    @CommandLine.Parameters(
        index = "0",
        description = "The name of the command.",
        defaultValue = CommandLine.Option.NULL_VALUE)
    private String commandName;

    public HelpCommand(@NonNull final CommandContext context) {
        super(context);
    }

    @Override
    public Integer call() {
        if (commandName == null) {
            // Display the usage of the chamber
            return CommandLine.ExitCode.OK;
        }

        final var command = context.getProcessor().getCommand(commandName);
        if (command == null) {
            buffer.append("No such command: ").append(commandName);
        } else {
            buffer.append(new CommandLine(command).getUsageMessage());
        }

        return CommandLine.ExitCode.OK;
    }
}