package burrow.furniture.standard;

import burrow.core.command.Command;
import burrow.core.command.CommandContext;
import burrow.core.command.CommandType;
import burrow.core.common.ColorUtility;
import burrow.core.furniture.BurrowFurniture;
import picocli.CommandLine;

import java.util.ArrayList;

@CommandLine.Command(name = "flist", description = "Prints the list of furniture of this chamber.")
@CommandType(StandardFurniture.COMMAND_TYPE)
public class FurnitureListCommand extends Command {
    public FurnitureListCommand(final CommandContext commandContext) {
        super(commandContext);
    }

    @Override
    public Integer call() {
        final var standardFurniture = use(StandardFurniture.class);
        final var lines = new ArrayList<String>();
        final var furnitureClassList = standardFurniture.getFurnitureClassList();

        var i = 0;
        for (final var furnitureClass : furnitureClassList) {
            final var name = furnitureClass.getName();
            final var simpleName = furnitureClass.getAnnotation(BurrowFurniture.class).simpleName();
            final var coloredSimpleName =
                ColorUtility.render(simpleName, ColorUtility.Type.NAME_FURNITURE);
            lines.add(String.format("[%d] %s (%s)", i++, coloredSimpleName, name));
        }

        bufferAppendLines(lines);

        return CommandLine.ExitCode.OK;
    }
}
