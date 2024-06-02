package me.jameschan.burrow.kernel.command.entry;

import me.jameschan.burrow.kernel.command.Command;
import me.jameschan.burrow.kernel.common.ExitCode;
import me.jameschan.burrow.kernel.context.RequestContext;
import me.jameschan.burrow.kernel.furniture.annotation.CommandType;
import picocli.CommandLine;

@CommandLine.Command(name = "count", description = "Retrieve the count of entries.")
@CommandType(CommandType.ENTRY)
public class CountCommand extends Command {
  public CountCommand(final RequestContext requestContext) {
    super(requestContext);
  }

  @Override
  public Integer call() throws Exception {
    final var count = context.getHoard().getAllEntries().size();
    buffer.append(count);

    return ExitCode.SUCCESS;
  }
}
