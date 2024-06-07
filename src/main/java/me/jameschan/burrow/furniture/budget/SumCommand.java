package me.jameschan.burrow.furniture.budget;

import java.time.LocalDate;
import java.time.ZoneId;
import me.jameschan.burrow.furniture.keyvalue.KeyValueFurniture;
import me.jameschan.burrow.kernel.command.Command;
import me.jameschan.burrow.kernel.common.ExitCode;
import me.jameschan.burrow.kernel.context.ChamberContext;
import me.jameschan.burrow.kernel.context.RequestContext;
import me.jameschan.burrow.kernel.furniture.annotation.CommandType;
import org.springframework.lang.NonNull;
import picocli.CommandLine;

@CommandLine.Command(
    name = "sum",
    description = "Find the sum of all records after a specified number of days ago.")
@CommandType("Budget")
public class SumCommand extends Command {
  @CommandLine.Parameters(
      index = "0",
      description = "Number of days ago from which to start collecting entries.")
  private Integer days;

  @CommandLine.Option(
      names = {"-c", "--category"},
      description =
          "Category of entries to collect. If not specified, all categories are collected.",
      defaultValue = CommandLine.Option.NULL_VALUE)
  private String category;

  public SumCommand(final RequestContext requestContext) {
    super(requestContext);
  }

  @Override
  public Integer call() {
    final var targetDate = LocalDate.now().minusDays(days);
    final var timestamp =
        targetDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
    final var sum =
        category == null
            ? getSum(context, timestamp)
            : getSumByCategory(context, timestamp, category);
    buffer.append(sum);

    return ExitCode.SUCCESS;
  }

  public static double getSum(@NonNull final ChamberContext chamberContext, final long timestamp) {
    final var collection = CollectCommand.collectEntriesCreatedAfter(chamberContext, timestamp);
    return collection.stream()
        .map(KeyValueFurniture::getValue)
        .mapToDouble(Double::parseDouble)
        .reduce(0.0, Double::sum);
  }

  public static double getSumByCategory(
      @NonNull final ChamberContext chamberContext,
      final long timestamp,
      @NonNull final String category) {
    final var collection =
        CollectCommand.collectEntriesByCategoryCreatedAfter(chamberContext, timestamp, category);
    return collection.stream()
        .map(KeyValueFurniture::getValue)
        .mapToDouble(Double::parseDouble)
        .reduce(0.0, Double::sum);
  }
}
