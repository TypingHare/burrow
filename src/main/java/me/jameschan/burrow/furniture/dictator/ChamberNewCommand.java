package me.jameschan.burrow.furniture.dictator;

import me.jameschan.burrow.kernel.Chamber;
import me.jameschan.burrow.kernel.ChamberShepherd;
import me.jameschan.burrow.kernel.command.Command;
import me.jameschan.burrow.kernel.common.ExitCode;
import me.jameschan.burrow.kernel.config.Config;
import me.jameschan.burrow.kernel.context.ChamberContext;
import me.jameschan.burrow.kernel.context.RequestContext;
import me.jameschan.burrow.kernel.furniture.annotation.CommandType;
import picocli.CommandLine;

@CommandLine.Command(name = "rnew", description = "Creates a new chamber")
@CommandType(CommandType.BUILTIN)
public class ChamberNewCommand extends Command {
  @CommandLine.Parameters(index = "0", description = "The name of the new chamber.")
  private String name;

  @CommandLine.Parameters(index = "1", description = "The description of the new chamber.")
  private String description;

  public ChamberNewCommand(final RequestContext requestContext) {
    super(requestContext);
  }

  @Override
  public Integer call() throws Exception {
    final var chamberList = DictatorFurniture.getAllChambers();
    if (chamberList.contains(name)) {
      buffer.append("Chamber already exists.");
      return ExitCode.ERROR;
    }

    final var newChamberRootDir = ChamberShepherd.CHAMBER_ROOT_DIR.resolve(name);
    if (!newChamberRootDir.toFile().mkdirs()) {
      buffer.append("Fail to create chamber root directory.");
      return ExitCode.ERROR;
    }

    // Create a config file
    final var applicationContext = context.getChamber().getApplicationContext();
    final var chamber = applicationContext.getBean(Chamber.class);

    // Create a config and set the chamber's name and description
    final var config = applicationContext.getBean(Config.class, chamber);
    config.set(Config.Key.CHAMBER_NAME, name);
    config.set(Config.Key.CHAMBER_DESCRIPTION, description);

    // Create a new config file
    final var configFile = newChamberRootDir.resolve(Config.CONFIG_FILE_NAME).normalize().toFile();
    chamber.getContext().set(ChamberContext.Key.CONFIG_FILE, configFile);
    config.saveToFile();

    return ExitCode.SUCCESS;
  }
}
