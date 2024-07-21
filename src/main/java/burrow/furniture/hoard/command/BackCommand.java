package burrow.furniture.hoard.command;

import burrow.core.command.Command;
import burrow.core.command.CommandContext;
import burrow.core.command.CommandType;
import burrow.furniture.hoard.HoardFurniture;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;


@CommandLine.Command(
    name = "back",
    description = "Create a backup for the hoard."
)
@CommandType(HoardFurniture.COMMAND_TYPE)
public class BackCommand extends Command {
    @CommandLine.Parameters(
        index = "0",
        description = "The name of the backup file.",
        defaultValue = "default"
    )
    private String name;

    public BackCommand(@NotNull final CommandContext commandContext) {
        super(commandContext);
    }

    @Override
    public Integer call() {
        final var fileName = use(HoardFurniture.class).backup(name);
        buffer.append("Backup file created: ").append(fileName);

        return CommandLine.ExitCode.OK;
    }
}
