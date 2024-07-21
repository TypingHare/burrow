package burrow.furniture.hoard.command;

import burrow.core.command.Command;
import burrow.core.command.CommandContext;
import burrow.core.command.CommandType;
import burrow.furniture.hoard.HoardFurniture;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

@CommandLine.Command(
    name = "entry",
    description = "Find an entry by its associated ID and display it."
)
@CommandType(HoardFurniture.COMMAND_TYPE)
public class EntryCommand extends Command {
    @CommandLine.Parameters(index = "0")
    private Integer id;

    @CommandLine.Option(
        names = {"-o", "--object"},
        description = "Display it as an entry object.",
        defaultValue = "false"
    )
    private Boolean asObject;

    public EntryCommand(@NotNull final CommandContext commandContext) {
        super(commandContext);
    }

    @Override
    public Integer call() {
        final var hoardFurniture = use(HoardFurniture.class);
        final var entry = hoardFurniture.getHoard().get(id);

        if (asObject) {
            final var hoard = hoardFurniture.getHoard();
            final var entryObject = hoard.getEntryObject(entry);
            final var environment = CommandContext.Hook.environment.getNotNull(commandContext);
            final var string = hoard.format(id, entryObject, environment);
            buffer.append(string);
        } else {
            buffer.append(hoardFurniture.entryToString(entry, commandContext));
        }

        return CommandLine.ExitCode.OK;
    }
}
