package me.jameschan.burrow.furniture.finder;

import java.util.ArrayList;
import me.jameschan.burrow.furniture.keyvalue.KeyValueFurniture;
import me.jameschan.burrow.kernel.command.Command;
import me.jameschan.burrow.kernel.common.ExitCode;
import me.jameschan.burrow.kernel.context.RequestContext;
import picocli.CommandLine;

@CommandLine.Command(name = "search", description = "Search for directories.")
public class SearchCommand extends Command {
  @CommandLine.Parameters(
      index = "0",
      paramLabel = "<directory-name>",
      description = "The name of directories to search for.")
  private String directoryName;

  public SearchCommand(final RequestContext requestContext) {
    super(requestContext);
  }

  @Override
  public Integer call() throws Exception {
    final var hoard = context.getHoard();
    final var keyValueFurniture = getFurniture(KeyValueFurniture.class);
    final var idList = keyValueFurniture.getIdSetByKey(directoryName);
    final var pathList =
        idList.stream()
            .map(hoard::getById)
            .map(entry -> KeyValueFurniture.getValue(entry, keyValueFurniture))
            .toList();

    if (pathList.isEmpty()) {
      buffer.append("No match results.");
    } else if (pathList.size() == 1) {
      // Exactly one match result
      buffer.append(pathList.getFirst());
    } else {
      // Multiple match results
      buffer.append(pathList.size()).append(" matching results found:\n");
      final var lines = new ArrayList<String>();
      for (var i = 0; i < pathList.size(); i++) {
        lines.add("[" + i + "] " + pathList.get(i));
      }

      bufferAppendLines(lines);
    }

    return ExitCode.SUCCESS;
  }
}
