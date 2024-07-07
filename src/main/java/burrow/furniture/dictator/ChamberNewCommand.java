package burrow.furniture.dictator;

import burrow.core.Burrow;
import burrow.core.chamber.Chamber;
import burrow.core.chamber.ChamberInitializationException;
import burrow.core.command.Command;
import burrow.core.command.CommandContext;
import burrow.core.command.CommandType;
import burrow.core.config.Config;
import burrow.furniture.aspectcore.AspectCoreFurniture;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.lang.NonNull;
import picocli.CommandLine;

import java.io.IOException;

@CommandLine.Command(name = "rnew", description = "Create a new chamber.")
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
    public Integer call() throws IOException, ChamberInitializationException {
        final var chamberList = use(DictatorFurniture.class).getAvailableChamberList();
        if (chamberList.contains(name)) {
            buffer.append("Chamber already exists.");
            return CommandLine.ExitCode.USAGE;
        }

        final var newChamberRootDir = Burrow.CHAMBERS_ROOT_DIR.resolve(name);
        if (!newChamberRootDir.toFile().mkdirs()) {
            buffer.append("Fail to create chamber root directory.");
            return CommandLine.ExitCode.SOFTWARE;
        }

        try {
            final var aspectCoreFurniture = use(AspectCoreFurniture.class);
            final var chamber = new Chamber(aspectCoreFurniture.getChamberShepherd(), name);
            final var newConfig = new Config(chamber);
            final var newConfigPath = Burrow.CHAMBERS_ROOT_DIR
                .resolve(name).resolve(Config.CONFIG_FILE_NAME);
            final var defaultFurnitureList = getConfig()
                .getNonNull(DictatorFurniture.ConfigKey.DICTATOR_DEFAULT_FURNITURE_LIST);
            newConfig.set(Config.Key.CHAMBER_NAME, name);
            newConfig.set(Config.Key.CHAMBER_DESCRIPTION, description);
            newConfig.set(Config.Key.CHAMBER_FURNITURE_LIST, defaultFurnitureList);

            final var newChamberContext = chamber.getChamberContext();
            newChamberContext.setConfigPath(newConfigPath);
            newChamberContext.setConfig(newConfig);
            newConfig.saveToFile();
        } catch (final Throwable ex) {
            buffer.append("Fail to create a new chamber.");
            bufferAppendThrowable(ex);

            // Remove new chamber root dir
            FileUtils.deleteDirectory(newChamberRootDir.toFile());
        }

        return CommandLine.ExitCode.OK;
    }
}
