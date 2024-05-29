package me.jameschan.burrow.kernel.command.entry;

import java.util.Arrays;
import java.util.List;
import me.jameschan.burrow.kernel.command.Command;
import me.jameschan.burrow.kernel.common.ExitCode;
import me.jameschan.burrow.kernel.context.RequestContext;
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
      return ExitCode.ERROR;
    }

    final var hoard = context.getHoard();
    final var formatter = context.getFormatter();
    final List<String> entryStringList =
        Arrays.stream(idListString.substring(1, idListString.length() - 1).split(","))
            .map(String::trim)
            .map(Integer::parseInt)
            .map(hoard::getById)
            .map(formatter::format)
            .toList();
    buffer.append(formatter.format(entryStringList));

    return ExitCode.SUCCESS;
  }
}
