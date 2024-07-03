package burrow.furniture.wordy;

import burrow.core.chamber.Chamber;
import burrow.core.chamber.ChamberContext;
import burrow.core.common.Values;
import burrow.core.config.Config;
import burrow.core.entry.Entry;
import burrow.core.furniture.BurrowFurniture;
import burrow.core.furniture.Furniture;
import burrow.furniture.keyvalue.KeyValueFurniture;
import burrow.furniture.time.TimeFurniture;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.Random;

@BurrowFurniture(
    simpleName = "Wordy",
    description = "Learn and review vocabulary with Wordy!",
    dependencies = {
        KeyValueFurniture.class,
        TimeFurniture.class
    }
)
public class WordyFurniture extends Furniture {
    public static final String COMMAND_TYPE = "Wordy";

    private final Random random = new Random();
    private int lastWordId = 0;

    public WordyFurniture(@NonNull final Chamber chamber) {
        super(chamber);
    }

    @Override
    public void init() {
        registerCommand(NewCommand.class);
        registerCommand(NextCommand.class);
        registerCommand(ArchiveCommand.class);
        registerCommand(ArchiveLastCommand.class);
    }

    @Override
    public void initConfig(@NonNull final Config config) {
        config.set(KeyValueFurniture.ConfigKey.KV_KEY_NAME, EntryKey.WORD);
        config.set(KeyValueFurniture.ConfigKey.KV_VALUE_NAME, EntryKey.TRANSLATION);
    }

    @Nullable
    public Entry getNextWord() {
        final var hoard = context.getHoard();
        if (hoard.getSize() == 0) {
            return null;
        }

        final var allEntries =
            hoard.getAllEntries().stream().filter(entry -> entry.isFalse(EntryKey.IS_ARCHIVED))
                .toList();
        final var id = random.nextInt(allEntries.size());
        final var wordEntry = allEntries.get(id);

        // Update reviews and time
        wordEntry.set(EntryKey.REVIEWS, wordEntry.getInt(EntryKey.REVIEWS, 0) + 1);
        TimeFurniture.setUpdateTime(context, wordEntry);

        // Save the id of the entry
        lastWordId = wordEntry.getId();

        return wordEntry;
    }

    public int getLastWordId() {
        return lastWordId;
    }

    @NonNull
    public Entry createEntry(
        @NonNull final String word,
        @NonNull final String translation
    ) {
        final var keyValueFurniture = use(KeyValueFurniture.class);
        final var entry = keyValueFurniture.createEntryWithKeyValue(word, translation);
        entry.set(WordyFurniture.EntryKey.EXAMPLE, Values.EMPTY);
        entry.set(WordyFurniture.EntryKey.IS_ARCHIVED, Values.Bool.FALSE);
        entry.set(WordyFurniture.EntryKey.REVIEWS, Values.Int.ZERO);

        return entry;
    }

    public static void setExample(@NonNull final Entry entry, @NonNull final String example) {
        entry.set(WordyFurniture.EntryKey.EXAMPLE, example);
    }

    public static void archive(
        @NonNull final ChamberContext chamberContext,
        @NonNull final Entry entry
    ) {
        entry.set(WordyFurniture.EntryKey.IS_ARCHIVED, Values.Bool.TRUE);
        TimeFurniture.setUpdateTime(chamberContext, entry);
    }

    public static void archiveLastWord(@NonNull final ChamberContext chamberContext) {
        final var wordyFurniture = chamberContext.getRenovator().getFurniture(WordyFurniture.class);
        final var hoard = chamberContext.getHoard();
        final var lastWordId = wordyFurniture.getLastWordId();
        if (!hoard.exist(lastWordId)) {
            return;
        }

        final var lastWord = hoard.getById(lastWordId);
        archive(chamberContext, lastWord);
    }

    public @interface EntryKey {
        String WORD = "word";
        String TRANSLATION = "translation";
        String EXAMPLE = "example";
        String IS_ARCHIVED = "is_archived";
        String REVIEWS = "reviews";
    }
}
