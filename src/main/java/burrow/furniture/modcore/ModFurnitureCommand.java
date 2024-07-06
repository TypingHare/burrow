package burrow.furniture.modcore;

import burrow.core.command.Command;
import burrow.core.command.CommandContext;
import burrow.core.command.CommandType;
import burrow.core.furniture.Furniture;
import org.springframework.lang.NonNull;
import picocli.CommandLine;

import java.util.ArrayList;

@CommandLine.Command(
    name = "modf",
    description = "Display all furniture each mod has."
)
@CommandType(ModCoreFurniture.COMMAND_TYPE)
public final class ModFurnitureCommand extends Command {
    public ModFurnitureCommand(@NonNull final CommandContext commandContext) {
        super(commandContext);
    }

    @Override
    public Integer call() {
        final var registrar = getRenovator().getRegistrar();
        final var furnitureMap = registrar.getFurnitureMap();
        final var modCoreFurniture = use(ModCoreFurniture.class);
        final var modPathList = modCoreFurniture.getModPathList();

        var i = 0;
        final var lines = new ArrayList<String>();
        for (final var modPath : modPathList) {
            lines.add(String.format("[%d] %s", i++, ModListCommand.getModPathLine(modPath)));

            final var classLoader = modCoreFurniture.getPathClassLoaderMap().get(modPath);
            var j = 0;
            for (final var furniture : furnitureMap.get(classLoader)) {
                lines.add(String.format("    [%d] %s (%s)", j++,
                    Furniture.getSimpleName(furniture), furniture.getName()));
            }
        }

        bufferAppendLines(lines);

        return CommandLine.ExitCode.OK;
    }
}