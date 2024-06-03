package me.jameschan.burrow.kernel.command.builtin;

import me.jameschan.burrow.kernel.command.Command;
import me.jameschan.burrow.kernel.common.ExitCode;
import me.jameschan.burrow.kernel.config.Config;
import me.jameschan.burrow.kernel.context.ChamberContext;
import me.jameschan.burrow.kernel.context.RequestContext;
import me.jameschan.burrow.kernel.furniture.annotation.CommandType;
import me.jameschan.burrow.kernel.utility.ColorUtility;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import picocli.CommandLine;

@CommandLine.Command(
    name = "description",
    description = "Retrieve or update the current chamber's description.")
@CommandType(CommandType.BUILTIN)
public class DescriptionCommand extends Command {
  @CommandLine.Parameters(
      index = "0",
      paramLabel = "<description>",
      description = "The new description.",
      defaultValue = CommandLine.Option.NULL_VALUE)
  private String newDescription;

  public DescriptionCommand(final RequestContext requestContext) {
    super(requestContext);
  }

  @Override
  public Integer call() throws Exception {
    if (newDescription == null) {
      final var description = retrieveDescription(context);
      if (description == null) {
        buffer.append(ColorUtility.render("null", ColorUtility.Type.NULL));
      } else {
        buffer.append(ColorUtility.render(description, ColorUtility.Type.DESCRIPTION));
      }
    } else {
      updateDescription(context, newDescription);
    }

    return ExitCode.SUCCESS;
  }

  @Nullable
  public static String retrieveDescription(@NonNull final ChamberContext context) {
    return context.getConfig().get(Config.Key.CHAMBER_DESCRIPTION);
  }

  public static void updateDescription(
      @NonNull final ChamberContext context, @NonNull final String description) {
    context.getConfig().set(Config.Key.CHAMBER_DESCRIPTION, description);
  }
}
