package me.jameschan.burrow.kernel.command.chamber;

import me.jameschan.burrow.kernel.command.Command;
import me.jameschan.burrow.kernel.common.ExitCode;
import me.jameschan.burrow.kernel.config.Config;
import me.jameschan.burrow.kernel.context.RequestContext;
import me.jameschan.burrow.kernel.utility.ColorUtility;
import picocli.CommandLine;

@CommandLine.Command(name = "description", description = "Retrieve or update description.")
public class DescriptionCommand extends Command {
  @CommandLine.Parameters(
      index = "0",
      description = "The new description.",
      defaultValue = CommandLine.Option.NULL_VALUE)
  private String description;

  public DescriptionCommand(final RequestContext requestContext) {
    super(requestContext);
  }

  @Override
  public Integer call() throws Exception {
    if (description == null) {
      // Retrieve the description
      final var description = context.getConfig().get(Config.Key.CHAMBER_DESCRIPTION);
      if (description == null) {
        buffer.append(ColorUtility.render("null", ColorUtility.Type.NULL));
      } else {
        buffer.append(ColorUtility.render(description, ColorUtility.Type.DESCRIPTION));
      }
    } else {
      // Update description
      context.getConfig().set(Config.Key.CHAMBER_DESCRIPTION, description);
    }

    return ExitCode.SUCCESS;
  }
}
