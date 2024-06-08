package me.jameschan.burrow.kernel.command.builtin;

import me.jameschan.burrow.kernel.command.Command;
import me.jameschan.burrow.kernel.common.ExitCode;
import me.jameschan.burrow.kernel.context.RequestContext;
import me.jameschan.burrow.kernel.furniture.annotation.CommandType;
import picocli.CommandLine;

@CommandLine.Command(name = "hoard", description = "Everything related to the entire hoard.")
@CommandType(CommandType.BUILTIN)
public class HoardCommand extends Command {
  @CommandLine.Option(
      names = {"-e", "--empty"},
      description = "Empty the hoard",
      defaultValue = "false")
  private boolean empty;

  public HoardCommand(final RequestContext requestContext) {
    super(requestContext);
  }

  @Override
  public Integer call() {
    if (empty) {
      final var hoard = context.getHoard();
      hoard.clearAll();
      hoard.saveToFile();
    }

    return ExitCode.SUCCESS;
  }
}
