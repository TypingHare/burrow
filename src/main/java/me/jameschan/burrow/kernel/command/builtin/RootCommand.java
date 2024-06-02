package me.jameschan.burrow.kernel.command.builtin;

import me.jameschan.burrow.kernel.command.Command;
import me.jameschan.burrow.kernel.common.ExitCode;
import me.jameschan.burrow.kernel.context.ChamberContext;
import me.jameschan.burrow.kernel.context.RequestContext;
import me.jameschan.burrow.kernel.furniture.annotation.CommandType;
import picocli.CommandLine;

@CommandLine.Command(name = "root", description = "Print the root directory of the application.")
@CommandType(CommandType.BUILTIN)
public class RootCommand extends Command {
  public RootCommand(final RequestContext requestContext) {
    super(requestContext);
  }

  @Override
  public Integer call() {
    buffer.append(getRootDirectoryAbsolutePath(context));
    return ExitCode.SUCCESS;
  }

  public static String getRootDirectoryAbsolutePath(final ChamberContext context) {
    return context.getRootDir().toString();
  }
}
