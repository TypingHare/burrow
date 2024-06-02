package me.jameschan.burrow.kernel.command.special;

import me.jameschan.burrow.kernel.command.Command;
import me.jameschan.burrow.kernel.command.Processor;
import me.jameschan.burrow.kernel.context.RequestContext;
import me.jameschan.burrow.kernel.furniture.annotation.CommandType;
import me.jameschan.burrow.kernel.utility.CommandUtility;
import picocli.CommandLine;

@CommandLine.Command(
    name = Processor.UNKNOWN_COMMAND_NAME,
    description = "A special command that is executed when an unknown command is used.")
@CommandType(CommandType.SPECIAL)
public class UnknownCommand extends Command {
  @CommandLine.Parameters(arity = "0..*")
  private String[] args;

  public UnknownCommand(final RequestContext requestContext) {
    super(requestContext);
  }

  @Override
  public Integer call() {
    buffer.append("Unknown command: ").append(requestContext.getCommandName());
    if (args != null && args.length > 0) {
      buffer.append("\n").append("Arguments: ").append(CommandUtility.getOriginalCommand(args));
    }

    return 0;
  }
}
