package burrow.furniture.hoard.command;

import burrow.core.chamber.ChamberInitializationException;
import burrow.core.command.Command;
import burrow.core.command.CommandContext;
import burrow.core.command.CommandType;
import burrow.furniture.hoard.HoardFurniture;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

import java.io.IOException;

@CommandLine.Command(
    name = "back.restore",
    description = "Restore the hoard with a specified backup."
)
@CommandType(HoardFurniture.COMMAND_TYPE)
public class BackRestoreCommand extends Command {
    @CommandLine.Parameters(
        index = "0",
        paramLabel = "<file-name>",
        description = "The file name of the backup used to restore."
    )
    private String fileName;

    @CommandLine.Option(
        names = {"--delete", "-d"},
        description = "Whether delete the backup file after restoring.",
        defaultValue = "false"
    )
    private boolean delete;

    public BackRestoreCommand(@NotNull final CommandContext commandContext) {
        super(commandContext);
    }

    @Override
    public Integer call() throws IOException, ChamberInitializationException {
        final var hoardFurniture = use(HoardFurniture.class);
        final var filePath = getChamberContext().getRootPath().resolve(fileName);
        final var isSuccess = hoardFurniture.restore(filePath);

        if (isSuccess) {
            buffer.append("Restored the hoard with a specified backup: ").append(fileName);
            if (delete) {
                final var isDeleteSuccess = hoardFurniture.deleteBackup(filePath);
                if (isDeleteSuccess) {
                    buffer.append("\n").append("Deleted backup file: ").append(fileName);
                }
            }

            // Restart the chamber without saving the current hoard
            hoardFurniture.setSaveHoardWhenTerminate(false);
            getChamber().restart();
        } else {
            buffer.append("Fail to restore the hoard with a specified backup: ").append(fileName);
        }

        return CommandLine.ExitCode.OK;
    }
}
