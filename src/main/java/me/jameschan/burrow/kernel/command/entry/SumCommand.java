package me.jameschan.burrow.kernel.command.entry;

import me.jameschan.burrow.kernel.command.Command;
import me.jameschan.burrow.kernel.common.ExitCode;
import me.jameschan.burrow.kernel.context.RequestContext;
import me.jameschan.burrow.kernel.furniture.annotation.CommandType;
import picocli.CommandLine;

@CommandLine.Command(name = "sum", description = "Find the summation of a certain property.")
@CommandType(CommandType.ENTRY)
public class SumCommand extends Command {
  @CommandLine.Parameters(index = "0", description = "The ")
  private String key;

  public SumCommand(final RequestContext requestContext) {
    super(requestContext);
  }

  @Override
  public Integer call() {
    final var entries = context.getHoard().getAllEntries();
    final var sum =
        entries.stream()
            .map(entry -> entry.getProperties().getOrDefault(key, "0"))
            .mapToDouble(Double::parseDouble)
            .reduce(0.0, Double::sum);

    buffer.append(sum);

    return ExitCode.SUCCESS;
  }
}
