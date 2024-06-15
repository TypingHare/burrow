package me.jameschan.burrow.furniture.wordy;

import java.util.Random;
import me.jameschan.burrow.furniture.keyvalue.KeyValueFurniture;
import me.jameschan.burrow.furniture.time.TimeFurniture;
import me.jameschan.burrow.kernel.Chamber;
import me.jameschan.burrow.kernel.config.Config;
import me.jameschan.burrow.kernel.entry.Entry;
import me.jameschan.burrow.kernel.furniture.Furniture;
import me.jameschan.burrow.kernel.furniture.annotation.BurrowFurniture;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

@BurrowFurniture(
    simpleName = "wordy",
    description = "A wordy furniture",
    dependencies = {KeyValueFurniture.class, TimeFurniture.class})
public class WordyFurniture extends Furniture {
  public static final String COMMAND_TYPE = "Wordy";

  private Entry word = null;
  private final Random random = new Random();

  public WordyFurniture(final Chamber chamber) {
    super(chamber);
  }

  @Override
  public void initConfig(@NonNull final Config config) {
    config.set(KeyValueFurniture.ConfigKey.KV_KEY_NAME, EntryKey.WORD);
    config.set(KeyValueFurniture.ConfigKey.KV_VALUE_NAME, EntryKey.TRANSLATION);
  }

  @Override
  public void init() {
    registerCommand(NewCommand.class);
    registerCommand(NextCommand.class);
    registerCommand(ArchiveLastCommand.class);
  }

  @Nullable
  public Entry getNextWord() {
    final var hoard = context.getHoard();
    if (hoard.getSize() == 0) {
      return null;
    }

    final var allEntries =
        hoard.getAllEntries().stream()
            .filter(entry -> entry.isFalse(EntryKey.IS_ARCHIVED))
            .toList();
    final var id = random.nextInt(allEntries.size());
    word = allEntries.get(id);

    // Update review times
    word.set(EntryKey.REVIEW_TIMES, word.getInt(EntryKey.REVIEW_TIMES, 0) + 1);
    TimeFurniture.updateEntry(context, word);

    return word;
  }

  @Nullable
  public Entry getLastWord() {
    return word;
  }

  public static final class EntryKey {
    public static final String WORD = "word";
    public static final String TRANSLATION = "translation";
    public static final String EXAMPLE = "example";
    public static final String IS_ARCHIVED = "is_archived";
    public static final String REVIEW_TIMES = "review_times";
  }
}
