package me.jameschan.burrow.kernel.command.builtin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import me.jameschan.burrow.kernel.command.Command;
import me.jameschan.burrow.kernel.common.ExitCode;
import me.jameschan.burrow.kernel.context.ChamberContext;
import me.jameschan.burrow.kernel.context.RequestContext;
import me.jameschan.burrow.kernel.furniture.annotation.CommandType;
import org.springframework.lang.NonNull;
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
  public Integer call() {
    if (full) {
      final var nameList = getFurnitureFullNameList(context);
      final var atomicInteger = new AtomicInteger(0);
      final var lines =
          nameList.stream()
              .map(name -> String.format("[%d] %s", atomicInteger.getAndIncrement(), name))
              .toList();
      bufferAppendLines(lines);
    } else {
      final var fullNameMap = getFurnitureFullNameMap(context);
      final var lines = new ArrayList<String>();

      var i = 0;
      for (final var name : fullNameMap.keySet()) {
        final var fullNameList = fullNameMap.get(name);
        if (fullNameList.size() == 1) {
          lines.add(String.format("[%d] %s", i++, name));
        } else {
          for (final String fullName : fullNameList) {
            lines.add(String.format("[%d] %s (%s)", i++, name, fullName));
          }
        }
      }
      bufferAppendLines(lines);
    }

    return ExitCode.SUCCESS;
  }

  @NonNull
  public static List<String> getFurnitureFullNameList(@NonNull final ChamberContext context) {
    return context.getRenovator().getAllFullNames();
  }

  @NonNull
  public static List<String> getFurnitureSimpleNameList(@NonNull final ChamberContext context) {
    return context.getRenovator().getFullNameMap().keySet().stream().toList();
  }

  @NonNull
  public static Map<String, List<String>> getFurnitureFullNameMap(
      @NonNull final ChamberContext context) {
    return context.getRenovator().getFullNameMap();
  }
}
