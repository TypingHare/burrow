package me.jameschan.burrow.kernel.command.builtin;

import me.jameschan.burrow.kernel.command.Command;
import me.jameschan.burrow.kernel.common.ExitCode;
import me.jameschan.burrow.kernel.context.RequestContext;
import picocli.CommandLine;

@CommandLine.Command(name = "root", description = "Print the root directory of the application.")
public class RootCommand extends Command {
  public RootCommand(final RequestContext requestContext) {
    super(requestContext);
  }

  @Override
  public Integer call() {
    buffer.append(context.getRootDir().toString());

    return ExitCode.SUCCESS;
  }
}
