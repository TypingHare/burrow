package me.jameschan.burrow.furniture.budget;

import me.jameschan.burrow.kernel.command.Command;
import me.jameschan.burrow.kernel.common.ExitCode;
import me.jameschan.burrow.kernel.context.ChamberContext;
import me.jameschan.burrow.kernel.context.RequestContext;
import me.jameschan.burrow.kernel.entry.Entry;
import me.jameschan.burrow.kernel.furniture.annotation.CommandType;
import org.springframework.lang.NonNull;
import picocli.CommandLine;

@CommandLine.Command(name = "new", description = "Create a new transaction.")
@CommandType(CommandType.ENTRY)
public class NewCommand extends Command {
  @CommandLine.Parameters(index = "0", description = "The category of the transaction.")
  private String category;

  @CommandLine.Parameters(index = "1", description = "The amount of the transaction.")
  private String amount;

  @CommandLine.Parameters(index = "2", description = "The description of the transaction.")
  private String description;

  public NewCommand(final RequestContext requestContext) {
    super(requestContext);
  }

  @Override
  public Integer call() {
    final var record = createRecord(context, category, amount, description);
    buffer.append(context.getFormatter().format(record));

    return ExitCode.SUCCESS;
  }

  @NonNull
  public static Entry createRecord(
      @NonNull final ChamberContext chamberContext,
      @NonNull final String category,
      @NonNull final String amount,
      @NonNull final String description) {
    final var entry =
        me.jameschan.burrow.furniture.keyvalue.NewCommand.createEntry(
            chamberContext, category, amount);
    entry.set(BudgetFurniture.EntryKey.DESCRIPTION, description);

    return entry;
  }
}
