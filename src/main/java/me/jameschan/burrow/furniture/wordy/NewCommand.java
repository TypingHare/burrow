package me.jameschan.burrow.furniture.wordy;

import me.jameschan.burrow.kernel.command.Command;
import me.jameschan.burrow.kernel.common.ExitCode;
import me.jameschan.burrow.kernel.context.ChamberContext;
import me.jameschan.burrow.kernel.context.RequestContext;
import me.jameschan.burrow.kernel.entry.Entry;
import me.jameschan.burrow.kernel.furniture.annotation.CommandType;
import org.springframework.lang.NonNull;
import picocli.CommandLine;

@CommandLine.Command(name = "new", description = "Include a new word.")
@CommandType(WordyFurniture.COMMAND_TYPE)
public class NewCommand extends Command {
  @CommandLine.Parameters(index = "0", description = "The original word.")
  private String word;

  @CommandLine.Parameters(index = "1", description = "The translation of the word.")
  private String translation;

  @CommandLine.Parameters(index = "2", description = "Example sentence.", defaultValue = "")
  private String example;

  public NewCommand(final RequestContext requestContext) {
    super(requestContext);
  }

  @Override
  public Integer call() {
    final var entry = createEntry(context, word, translation);
    if (!example.isEmpty()) {
      setExample(entry, example);
    }
    bufferAppendEntry(entry);

    return ExitCode.SUCCESS;
  }

  @NonNull
  public static Entry createEntry(
      @NonNull final ChamberContext chamberContext,
      @NonNull final String word,
      @NonNull final String translation) {
    final var entry =
        me.jameschan.burrow.furniture.keyvalue.NewCommand.createEntry(
            chamberContext, word, translation);
    entry.set(WordyFurniture.EntryKey.EXAMPLE, "");
    entry.set(WordyFurniture.EntryKey.IS_ARCHIVED, "false");
    entry.set(WordyFurniture.EntryKey.REVIEW_TIMES, "0");

    return entry;
  }

  public static void setExample(@NonNull final Entry entry, @NonNull final String example) {
    entry.set(WordyFurniture.EntryKey.EXAMPLE, example);
  }
}
