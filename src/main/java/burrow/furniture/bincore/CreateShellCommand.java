package burrow.furniture.bincore;

import burrow.core.command.Command;
import burrow.core.command.CommandContext;
import burrow.core.command.CommandType;
import org.springframework.lang.NonNull;
import picocli.CommandLine;

import java.io.IOException;

@CommandLine.Command(name = "create-shell", description = "Create a shell file.")
@CommandType(BinCoreFurniture.COMMAND_TYPE)
public class CreateShellCommand extends Command {
    @CommandLine.Option(
        names = {"--force", "-f"},
        paramLabel = "is-force",
        description = "Create a shell file, whether or not it already exists.",
        defaultValue = "false"
    )
    public Boolean isForce;

    public CreateShellCommand(@NonNull final CommandContext commandContext) {
        super(commandContext);
    }

    @Override
    public Integer call() throws IOException {
        final var binCoreFurniture = use(BinCoreFurniture.class);
        final var shellContent = binCoreFurniture.getDefaultShellContent();
        if (isForce) {
            binCoreFurniture.createShellFile(shellContent);
        } else {
            final var createdFile = binCoreFurniture.createShellFileIfAbsent(shellContent);
            if (!createdFile) {
                buffer.append("Shell file is not created because it already exists. Use --force option to create a new shell file.");
            }
        }

        return CommandLine.ExitCode.OK;
    }
}
