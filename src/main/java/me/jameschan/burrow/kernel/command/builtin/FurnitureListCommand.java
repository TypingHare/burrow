package me.jameschan.burrow.kernel.command.builtin;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import me.jameschan.burrow.kernel.command.Command;
import me.jameschan.burrow.kernel.common.ExitCode;
import me.jameschan.burrow.kernel.context.RequestContext;
import me.jameschan.burrow.kernel.furniture.annotation.CommandType;
import picocli.CommandLine;

@CommandLine.Command(name = "flist", description = "Prints the list of furniture of this chamber.")
@CommandType(CommandType.BUILTIN)
public class FurnitureListCommand extends Command {
  @CommandLine.Option(
      names = {"-f", "--full"},
      defaultValue = "false",
      description = "Whether print furniture list in simple-name form.")
  private Boolean full;

  public FurnitureListCommand(final RequestContext requestContext) {
    super(requestContext);
  }

  @Override
  public Integer call() throws Exception {
    final var renovator = context.getRenovator();
    final var nameList = full ? renovator.getAllFullNames() : renovator.getAllSimpleNames();

    if (!nameList.isEmpty()) {
      final var i = new AtomicInteger(0);
      final var lineList =
          nameList.stream()
              .map(name -> "[" + i.getAndIncrement() + "] " + name)
              .collect(Collectors.joining("\n"));
      buffer.append(lineList);
    }

    return ExitCode.SUCCESS;
  }
}
