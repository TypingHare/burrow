package me.jameschan.burrow.furniture.finder;

import me.jameschan.burrow.kernel.command.Command;
import me.jameschan.burrow.kernel.command.Processor;
import me.jameschan.burrow.kernel.common.ExitCode;
import me.jameschan.burrow.kernel.context.RequestContext;
import picocli.CommandLine;

@CommandLine.Command(name = Processor.DEFAULT_COMMAND_NAME)
public class DefaultCommand extends Command {
  public DefaultCommand(final RequestContext requestContext) {
    super(requestContext);
  }

  @Override
  public Integer call() throws Exception {
    final var workingDirectory = requestContext.getWorkingDirectory();

    return ExitCode.SUCCESS;
  }
}
