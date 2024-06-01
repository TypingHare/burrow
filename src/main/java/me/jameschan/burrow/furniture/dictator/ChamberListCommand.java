package me.jameschan.burrow.furniture.dictator;

import com.google.common.base.Strings;
import java.io.IOException;
import java.util.ArrayList;
import me.jameschan.burrow.kernel.ChamberShepherd;
import me.jameschan.burrow.kernel.command.Command;
import me.jameschan.burrow.kernel.common.ExitCode;
import me.jameschan.burrow.kernel.config.Config;
import me.jameschan.burrow.kernel.context.RequestContext;
import me.jameschan.burrow.kernel.furniture.annotation.CommandType;
import me.jameschan.burrow.kernel.utility.ColorUtility;
import picocli.CommandLine;

@CommandLine.Command(
    name = "rlist",
    description = "Print the list of all chambers with descriptions.")
@CommandType(CommandType.BUILTIN)
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
      final var configMap = Config.loadFromConfigFile(configFile);
      final var description = configMap.get(Config.Key.CHAMBER_DESCRIPTION);
      lines.add(
          ColorUtility.render(Strings.padEnd(chamberName, 16, ' '), ColorUtility.Type.CHAMBER)
              + ColorUtility.render(description, ColorUtility.Type.DESCRIPTION));
    }

    bufferAppendLines(lines);

    return ExitCode.SUCCESS;
  }
}
