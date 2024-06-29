package burrow.furniture.entry;

import burrow.core.command.Command;
import burrow.core.command.CommandContext;
import burrow.core.command.CommandType;
import burrow.core.common.ColorUtility;
import burrow.core.common.Values;
import org.springframework.lang.NonNull;
import picocli.CommandLine;

@CommandLine.Command(name = "entry", description = "Find an entry by its associated ID.")
@CommandType(EntryFurniture.COMMAND_TYPE)
public class EntryCommand extends Command {
    @CommandLine.Parameters(index = "0")
    private Integer id;

    public EntryCommand(@NonNull final CommandContext commandContext) {
        super(commandContext);
    }

    @Override
    public Integer call() {
        final var entry = context.getHoard().getById(id);
        try {
            buffer.append(EntryFurniture.entryToString(context, entry));
        } catch (final IndexOutOfBoundsException ex) {
            buffer.append(ColorUtility.render(Values.NULL, ColorUtility.Type.NULL));
        }

        return CommandLine.ExitCode.OK;
    }
}
