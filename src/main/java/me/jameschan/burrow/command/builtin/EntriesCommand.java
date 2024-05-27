package me.jameschan.burrow.command.builtin;

import java.util.Arrays;
import java.util.stream.Collectors;
import me.jameschan.burrow.command.Command;
import me.jameschan.burrow.context.RequestContext;
import picocli.CommandLine;

@CommandLine.Command(name = "entries", description = "Retrieve a list of entries.")
public class EntriesCommand extends Command {
  @CommandLine.Parameters(index = "0", description = "A list of IDs to retrieve.")
  private String idList;

  public EntriesCommand(final RequestContext context) {
    super(context);
  }

  @Override
  public Integer call() throws Exception {
    final var idListString = idList.trim();
    if (idListString.length() < 2) {
      buffer.append("Invalid id list string: ").append(idListString);
      return 0;
    }

    final var hoard = context.getHoard();
    final var entryListString =
        Arrays.stream(idListString.substring(1, idListString.length() - 1).split(","))
            .map(String::trim)
            .map(Integer::parseInt)
            .map(hoard::getById)
            .map(hoard::getFormattedEntryString)
            .collect(Collectors.joining(",\n"));

    buffer.append("[\n").append(entryListString).append("\n]");

    return 0;
  }
}
