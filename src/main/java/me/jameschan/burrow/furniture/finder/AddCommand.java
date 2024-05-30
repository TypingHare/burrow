package me.jameschan.burrow.furniture.finder;

import java.nio.file.Path;
import java.util.List;
import me.jameschan.burrow.kernel.command.Command;
import me.jameschan.burrow.kernel.context.RequestContext;
import picocli.CommandLine;

@CommandLine.Command(name = "add", description = "Add a directory.")
public class AddCommand extends Command {
  @CommandLine.Parameters(index = "0", paramLabel = "directory-path", description = "")
  private String directoryPath;

  public AddCommand(final RequestContext requestContext) {
    super(requestContext);
  }

  @Override
  public Integer call() throws Exception {
    final var path = Path.of(directoryPath);
    final var directoryName = path.getFileName();

    return executeOther("new", List.of(directoryName.toString(), path.toString()));
  }
}
