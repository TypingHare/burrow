package me.jameschan.burrow.furniture.wordy;

import me.jameschan.burrow.furniture.time.TimeFurniture;
import me.jameschan.burrow.kernel.command.Command;
import me.jameschan.burrow.kernel.common.ExitCode;
import me.jameschan.burrow.kernel.context.RequestContext;
import me.jameschan.burrow.kernel.furniture.annotation.CommandType;
import me.jameschan.burrow.kernel.utility.ColorUtility;
import picocli.CommandLine;

@CommandLine.Command(name = "next", description = "Print a random next word.")
@CommandType(WordyFurniture.COMMAND_TYPE)
public class NextCommand extends Command {
  public NextCommand(final RequestContext requestContext) {
    super(requestContext);
  }

  @Override
  public Integer call() {
    final var wordyFurniture = getFurniture(WordyFurniture.class);
    final var nextWord = wordyFurniture.getNextWord();
    if (nextWord == null) {
      buffer.append("No available words.");
    } else {
      final var word = nextWord.get(WordyFurniture.EntryKey.WORD);
      final var translation = nextWord.get(WordyFurniture.EntryKey.TRANSLATION);
      final var example = nextWord.get(WordyFurniture.EntryKey.EXAMPLE);
      final var reviewTimes = nextWord.get(WordyFurniture.EntryKey.REVIEW_TIMES);
      final var createdAt = nextWord.get(TimeFurniture.EntryKey.CREATED_AT);
      final var updatedAt = nextWord.get(TimeFurniture.EntryKey.UPDATED_AT);
      final var extraInfo =
          "review times: "
              + reviewTimes
              + " | included at: "
              + TimeFurniture.dateToString(context, Long.parseLong(createdAt))
              + " | last reviewed at: "
              + TimeFurniture.dateToString(context, Long.parseLong(updatedAt));

      buffer.append(ColorUtility.render(word, "green"));
      buffer.append("  ");
      buffer.append(ColorUtility.render(translation, "green"));
      buffer.append("\n");
      buffer.append(ColorUtility.render(example, "yellow"));
      buffer.append("\n");
      buffer.append(ColorUtility.render(extraInfo, "fg(247)"));
    }

    return ExitCode.SUCCESS;
  }
}
