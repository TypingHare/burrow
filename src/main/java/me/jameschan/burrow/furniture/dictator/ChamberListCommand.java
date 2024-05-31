package me.jameschan.burrow.furniture.dictator;

import com.google.common.base.Strings;
import java.io.IOException;
import java.util.ArrayList;
import me.jameschan.burrow.kernel.ChamberShepherd;
import me.jameschan.burrow.kernel.command.Command;
import me.jameschan.burrow.kernel.common.ExitCode;
import me.jameschan.burrow.kernel.config.Config;
import me.jameschan.burrow.kernel.context.RequestContext;
import picocli.CommandLine;

@CommandLine.Command(
    name = "chamber-list",
    description = "Print the list of all chambers with descriptions.")
public class ChamberListCommand extends Command {
  public ChamberListCommand(final RequestContext requestContext) {
    super(requestContext);
  }

  @Override
  public Integer call() throws IOException {
    final var chamberNameList = DictatorFurniture.getAllChambers();
    final var lines = new ArrayList<String>();
    for (final var chamberName : chamberNameList) {
      final var configFile =
          ChamberShepherd.CHAMBER_ROOT_DIR.resolve(chamberName).resolve(Config.CONFIG_FILE_NAME);
      System.out.println(ChamberShepherd.CHAMBER_ROOT_DIR);
      final var configMap = Config.loadFromConfigFile(configFile);
      final var description = configMap.get(Config.Key.CHAMBER_DESCRIPTION);
      lines.add(Strings.padEnd(chamberName, 16, ' ') + description);
    }

    bufferAppendLines(lines);

    return ExitCode.SUCCESS;
  }
}
