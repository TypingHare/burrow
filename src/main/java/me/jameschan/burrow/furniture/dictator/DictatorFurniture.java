package me.jameschan.burrow.furniture.dictator;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import me.jameschan.burrow.kernel.Chamber;
import me.jameschan.burrow.kernel.ChamberShepherd;
import me.jameschan.burrow.kernel.furniture.Furniture;
import me.jameschan.burrow.kernel.furniture.annotation.BurrowFurniture;

@BurrowFurniture(
    simpleName = "dictator",
    description = "Allows users to create, delete, and monitor chambers.")
public class DictatorFurniture extends Furniture {
  public static final String COMMAND_TYPE = "Dictator";

  public DictatorFurniture(final Chamber chamber) {
    super(chamber);
  }

  @Override
  public void init() {
    registerCommand(ChamberNewCommand.class);
    registerCommand(ChamberListCommand.class);
  }

  public static List<String> getAllChambers() throws IOException {
    final List<String> chamberList = new ArrayList<>();
    final var chamberRootDirString = ChamberShepherd.CHAMBER_ROOT_DIR.toString();
    try (final DirectoryStream<Path> stream =
        Files.newDirectoryStream(ChamberShepherd.CHAMBER_ROOT_DIR, Files::isDirectory)) {
      for (Path entry : stream) {
        chamberList.add(entry.toString().substring(1 + chamberRootDirString.length()));
      }
    }

    return chamberList;
  }
}
