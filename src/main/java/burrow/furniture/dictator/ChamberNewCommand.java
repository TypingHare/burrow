package burrow.furniture.dictator;

import burrow.core.chamber.ChamberShepherd;
import burrow.core.command.Command;
import burrow.core.command.CommandContext;
import burrow.core.command.CommandType;
import burrow.core.config.Config;
import org.springframework.lang.NonNull;
import picocli.CommandLine;

import java.io.IOException;

@CommandLine.Command(name = "rnew", description = "Creates a new chamber")
@CommandType(DictatorFurniture.COMMAND_TYPE)
public class ChamberNewCommand extends Command {
    @CommandLine.Parameters(index = "0", description = "The name of the new chamber.")
    private String name;

    @CommandLine.Parameters(index = "1", description = "The description of the new chamber.")
    private String description;

    public ChamberNewCommand(@NonNull final CommandContext commandContext) {
        super(commandContext);
    }

    @Override
    public Integer call() throws IOException {
        final var chamberList = DictatorFurniture.getAllChambers();
        if (chamberList.contains(name)) {
            buffer.append("Chamber already exists.");
            return CommandLine.ExitCode.USAGE;
        }

        final var newChamberRootDir = ChamberShepherd.CHAMBER_ROOT_DIR.resolve(name);
        if (!newChamberRootDir.toFile().mkdirs()) {
            buffer.append("Fail to create chamber root directory.");
            return CommandLine.ExitCode.SOFTWARE;
        }

        final var newConfig = DictatorFurniture.createNewConfig(context, newChamberRootDir);
        final var defaultFurnitureList = context.getConfig()
            .getRequireNotNull(DictatorFurniture.ConfigKey.DICTATOR_DEFAULT_FURNITURE_LIST);
        newConfig.set(Config.Key.CHAMBER_NAME, name);
        newConfig.set(Config.Key.CHAMBER_DESCRIPTION, description);
        newConfig.set(Config.Key.FURNITURE_LIST, defaultFurnitureList);
        newConfig.saveToFile();

        return CommandLine.ExitCode.OK;
    }
}
