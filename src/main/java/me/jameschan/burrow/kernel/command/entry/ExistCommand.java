package me.jameschan.burrow.kernel.command.entry;

import me.jameschan.burrow.kernel.command.Command;
import me.jameschan.burrow.kernel.common.ExitCode;
import me.jameschan.burrow.kernel.context.RequestContext;
import picocli.CommandLine;

@CommandLine.Command(name = "exist", description = "Check if an entry exists.")
public class ExistCommand extends Command {
  @CommandLine.Parameters(index = "0", description = "The id of the entry.")
  private Integer id;

  public ExistCommand(RequestContext context) {
    super(context);
  }

  @Override
  public Integer call() {
    buffer.append(context.getHoard().exist(id) ? "true" : "false");

    return ExitCode.SUCCESS;
  }
}
