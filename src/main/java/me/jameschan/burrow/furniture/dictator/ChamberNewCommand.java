package me.jameschan.burrow.furniture.dictator;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import me.jameschan.burrow.kernel.Chamber;
import me.jameschan.burrow.kernel.ChamberShepherd;
import me.jameschan.burrow.kernel.command.Command;
import me.jameschan.burrow.kernel.common.ExitCode;
import me.jameschan.burrow.kernel.config.Config;
import me.jameschan.burrow.kernel.context.ChamberContext;
import me.jameschan.burrow.kernel.context.RequestContext;
import picocli.CommandLine;

@CommandLine.Command(name = "chamber-new", description = "Creates a new chamber")
public class ChamberNewCommand extends Command {
  @CommandLine.Parameters(index = "0", description = "The name of the new chamber.")
  private String name;

  public ChamberNewCommand(final RequestContext requestContext) {
    super(requestContext);
  }

  @Override
  public Integer call() throws Exception {
    final var chamberList = getAllChambers();
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

    // Create a config
    final var config = applicationContext.getBean(Config.class, chamber);
    config.set(Config.Key.CHAMBER_NAME, name);
    config.set(Config.Key.CHAMBER_VERSION, "1.0.0");
    config.set(Config.Key.FURNITURE_LIST, "");

    // Create a new config file
    final var configFile = newChamberRootDir.resolve(Config.CONFIG_FILE_NAME).normalize().toFile();
    chamber.getContext().set(ChamberContext.Key.CONFIG_FILE, configFile);
    config.saveToFile();

    return ExitCode.SUCCESS;
  }

  public static List<String> getAllChambers() throws IOException {
    final List<String> chamberList = new ArrayList<>();
    final var chamberRootDirString = ChamberShepherd.CHAMBER_ROOT_DIR.toString();
    try (final DirectoryStream<Path> stream =
        Files.newDirectoryStream(ChamberShepherd.CHAMBER_ROOT_DIR, Files::isDirectory)) {
      for (Path entry : stream) {
        chamberList.add(entry.toString().substring(chamberRootDirString.length()));
      }
    }

    return chamberList;
  }
}
