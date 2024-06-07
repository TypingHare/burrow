package me.jameschan.burrow.furniture.budget;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import me.jameschan.burrow.furniture.keyvalue.KeyValueFurniture;
import me.jameschan.burrow.furniture.time.TimeFurniture;
import me.jameschan.burrow.kernel.command.Command;
import me.jameschan.burrow.kernel.common.ExitCode;
import me.jameschan.burrow.kernel.context.ChamberContext;
import me.jameschan.burrow.kernel.context.RequestContext;
import me.jameschan.burrow.kernel.entry.Entry;
import me.jameschan.burrow.kernel.furniture.annotation.CommandType;
import org.springframework.lang.NonNull;
import picocli.CommandLine;

@CommandLine.Command(
    name = "collect",
    description = "Collect entries that were created after a specified number of days ago.")
@CommandType("Budget")
public class CollectCommand extends Command {
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

  public CollectCommand(final RequestContext requestContext) {
    super(requestContext);
  }

  @Override
  public Integer call() {
    final var timestamp = getTimestampDaysAgo(days);
    var collection =
        category == null
            ? collectEntriesCreatedAfter(context, timestamp)
            : collectEntriesByCategoryCreatedAfter(context, timestamp, category);
    buffer.append(context.getFormatter().format(collection));

    return ExitCode.SUCCESS;
  }

  /**
   * Collects entries from the specified {@link ChamberContext} that were created after the given
   * timestamp.
   *
   * <p>This method filters all entries in the hoard of the provided {@code chamberContext},
   * returning only those entries whose creation time, as indicated by the {@code CREATED_AT} key,
   * is greater than the specified timestamp.
   *
   * @param chamberContext the context containing the hoard from which entries are to be collected;
   *     must not be null
   * @param timestamp the cutoff time; only entries created after this timestamp will be included in
   *     the result
   * @return a collection of entries created after the specified timestamp
   * @throws NullPointerException if {@code chamberContext} or any of its entries' {@code
   *     CREATED_AT} value is null
   * @throws NumberFormatException if the {@code CREATED_AT} value of any entry cannot be parsed as
   *     a long
   */
  public static List<Entry> collectEntriesCreatedAfter(
      @NonNull final ChamberContext chamberContext, final long timestamp) {
    final Predicate<Entry> filterFunction =
        entry ->
            Long.parseLong(Objects.requireNonNull(entry.get(TimeFurniture.EntryKey.CREATED_AT)))
                > timestamp;
    return chamberContext.getHoard().getAllEntries().stream().filter(filterFunction).toList();
  }

  /**
   * Collects entries from the specified {@link ChamberContext} that were created after the given
   * timestamp and belong to the specified category.
   *
   * @param chamberContext the context containing the hoard from which entries are to be collected;
   *     must not be null
   * @param timestamp the cutoff time; only entries created after this timestamp will be included in
   *     the result
   * @param category the category to filter entries by; must not be null
   * @return a list of entries created after the specified timestamp and belonging to the specified
   *     category
   * @throws NullPointerException if {@code chamberContext} or {@code category} is null
   */
  public static List<Entry> collectEntriesByCategoryCreatedAfter(
      @NonNull final ChamberContext chamberContext,
      final long timestamp,
      @NonNull final String category) {
    return collectEntriesCreatedAfter(chamberContext, timestamp).stream()
        .filter(entry -> KeyValueFurniture.getKey(entry).equals(category))
        .toList();
  }

  /**
   * Gets the timestamp in milliseconds at 00:00 N days ago.
   *
   * @param n the number of days ago
   * @return the timestamp in milliseconds at 00:00 N days ago
   */
  public static long getTimestampDaysAgo(final int n) {
    final var targetDate = LocalDate.now().minusDays(n);
    return targetDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
  }
}
