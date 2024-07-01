package burrow.furniture.wordy;

import burrow.core.command.Command;
import burrow.core.command.CommandContext;
import burrow.core.command.CommandType;
import burrow.core.common.ColorUtility;
import burrow.furniture.time.TimeFurniture;
import org.springframework.lang.NonNull;
import picocli.CommandLine;

@CommandLine.Command(name = "next", description = "Print a random next word.")
@CommandType(WordyFurniture.COMMAND_TYPE)
public class NextCommand extends Command {
    public NextCommand(@NonNull final CommandContext commandContext) {
        super(commandContext);
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
            final var reviews = nextWord.get(WordyFurniture.EntryKey.REVIEWS);
            final var createdAt = nextWord.get(TimeFurniture.EntryKey.CREATED_AT);
            final var updatedAt = nextWord.get(TimeFurniture.EntryKey.UPDATED_AT);
            final var extraInfo = "reviews: " + reviews
                + " | included at: "
                + TimeFurniture.dateToString(context, Long.parseLong(createdAt))
                + " | reviewed at: "
                + TimeFurniture.dateToString(context, Long.parseLong(updatedAt));

            buffer.append(ColorUtility.render(word, "green,bold"));
            buffer.append("  ");
            buffer.append(ColorUtility.render(translation, "green,bold"));
            buffer.append("\n");
            buffer.append(ColorUtility.render(example, "fg(222)"));
            buffer.append("\n");
            buffer.append(ColorUtility.render(extraInfo, "fg(247)"));
        }

        return CommandLine.ExitCode.OK;
    }
}
