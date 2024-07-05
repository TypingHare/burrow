package burrow.furniture.standard;

import burrow.core.command.Command;
import burrow.core.command.CommandContext;
import burrow.core.command.CommandType;
import burrow.core.common.ColorUtility;
import burrow.core.furniture.Furniture;
import org.springframework.lang.NonNull;
import picocli.CommandLine;

import java.util.ArrayList;
import java.util.Comparator;

@CommandLine.Command(
    name = "fall",
    description = "List all available furniture."
)
@CommandType(StandardFurniture.COMMAND_TYPE)
public class FurnitureAllCommand extends Command {
    public FurnitureAllCommand(@NonNull final CommandContext commandContext) {
        super(commandContext);
    }

    @Override
    public Integer call() {
        final var furnitureRegistrar = getRenovator().getRegistrar();
        final var allFurnitureClassList =
            furnitureRegistrar.getFurnitureClassSet().stream()
                .sorted(Comparator.comparing(Class::getName))
                .toList();
        final var furnitureClassList = use(StandardFurniture.class).getFurnitureClassList();

        final var lines = new ArrayList<String>();
        var i = 0;
        for (final var furnitureClass : allFurnitureClassList) {
            final var simpleName = Furniture.getSimpleName(furnitureClass);
            final var coloredSimpleName = furnitureClassList.contains(furnitureClass) ?
                ColorUtility.render(simpleName, ColorUtility.Type.NAME_FURNITURE) :
                simpleName;
            lines.add(String.format("[%s] %s (%s)",
                i++,
                coloredSimpleName,
                furnitureClass.getName())
            );
        }

        bufferAppendLines(lines);

        return CommandLine.ExitCode.OK;
    }
}
