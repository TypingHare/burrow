package me.jameschan.burrow.furniture.wordy;

import me.jameschan.burrow.kernel.command.Command;
import me.jameschan.burrow.kernel.common.ExitCode;
import me.jameschan.burrow.kernel.context.RequestContext;
import me.jameschan.burrow.kernel.entry.Entry;
import me.jameschan.burrow.kernel.furniture.annotation.CommandType;
import org.springframework.lang.NonNull;
import picocli.CommandLine;

@CommandLine.Command(name = "archive-last", description = "Display a random word.")
@CommandType(WordyFurniture.COMMAND_TYPE)
public class ArchiveLastCommand extends Command {
  public ArchiveLastCommand(final RequestContext requestContext) {
    super(requestContext);
  }

  @Override
  public Integer call() {
    final var wordyFurniture = getFurniture(WordyFurniture.class);
    final var lastWord = wordyFurniture.getLastWord();
    if (lastWord != null) {
      archive(lastWord);
    }

    return ExitCode.SUCCESS;
  }

  public static void archive(@NonNull final Entry entry) {
    entry.set(WordyFurniture.EntryKey.IS_ARCHIVED, "true");
  }
}
