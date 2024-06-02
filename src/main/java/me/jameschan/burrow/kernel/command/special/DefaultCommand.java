package me.jameschan.burrow.kernel.command.special;

import java.util.ArrayList;
import me.jameschan.burrow.kernel.command.Command;
import me.jameschan.burrow.kernel.command.Processor;
import me.jameschan.burrow.kernel.common.ExitCode;
import me.jameschan.burrow.kernel.config.Config;
import me.jameschan.burrow.kernel.context.RequestContext;
import me.jameschan.burrow.kernel.furniture.annotation.CommandType;
import picocli.CommandLine;

@CommandLine.Command(
    name = Processor.DEFAULT_COMMAND_NAME,
    description = "The default command that is executed when no command is specified.")
@CommandType(CommandType.SPECIAL)
public class DefaultCommand extends Command {
  @CommandLine.Option(
      names = {"-v", "--version"},
      description = "Display the name and version of the current chamber.")
  private boolean version = false;

  @CommandLine.Option(
      names = {"-h", "--help"},
      description = "Display the help information.")
  private boolean help = false;

  public DefaultCommand(final RequestContext requestContext) {
    super(requestContext);
  }

  @Override
  public Integer call() {
    final var config = context.getConfig();

    final var lines = new ArrayList<String>();
    if (version) {
      final var chamberName = context.getConfig().get(Config.Key.CHAMBER_NAME);
      final var version = config.get(Config.Key.CHAMBER_VERSION);
      lines.add(String.format("%s v%s", chamberName, version));
    }

    if (help) {
      final var description = config.get(Config.Key.CHAMBER_DESCRIPTION);
      lines.add(description);
    }

    bufferAppendLines(lines);

    return ExitCode.SUCCESS;
  }
}
