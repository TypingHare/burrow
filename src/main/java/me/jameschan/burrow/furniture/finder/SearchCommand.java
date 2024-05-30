package me.jameschan.burrow.furniture.finder;

import java.util.List;
import me.jameschan.burrow.kernel.command.Command;
import me.jameschan.burrow.kernel.context.RequestContext;
import picocli.CommandLine;

@CommandLine.Command(name = "search", description = "Search for directories.")
public class SearchCommand extends Command {
  @CommandLine.Parameters(
      index = "0",
      paramLabel = "directory-name",
      description = "The name of directories to search for.")
  private String directoryName;

  public SearchCommand(final RequestContext requestContext) {
    super(requestContext);
  }

  @Override
  public Integer call() throws Exception {
    return executeOther("values", List.of(directoryName));
  }
}
