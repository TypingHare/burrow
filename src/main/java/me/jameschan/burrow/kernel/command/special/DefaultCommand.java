package me.jameschan.burrow.kernel.command.special;

import me.jameschan.burrow.kernel.command.Command;
import me.jameschan.burrow.kernel.command.Processor;
import me.jameschan.burrow.kernel.common.ExitCode;
import me.jameschan.burrow.kernel.config.Config;
import me.jameschan.burrow.kernel.context.RequestContext;
import picocli.CommandLine;

@CommandLine.Command(
    name = Processor.DEFAULT_COMMAND_NAME,
    description = "Prints the help information of the application.")
public class DefaultCommand extends Command {
  @CommandLine.Option(
      names = {"-v", "--version"},
      description = "Print the name and version of the app being used.")
  private boolean version = false;

  @CommandLine.Option(
      names = {"-h", "--help"},
      description = "Print help information.")
  private boolean help = false;

  public DefaultCommand(final RequestContext requestContext) {
    super(requestContext);
  }

  @Override
  public Integer call() {
    final var config = context.getConfig();

    if (version) {
      final var chamberName = context.getConfig().get(Config.Key.CHAMBER_NAME);
      final var version = config.get(Config.Key.CHAMBER_VERSION);
      buffer.append(String.format("%s v%s", chamberName, version));
    }

    if (help) {
      buffer.append("There is nothing I can help you with for the time being.");
    }

    return ExitCode.SUCCESS;
  }
}
