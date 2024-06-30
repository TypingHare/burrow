package burrow.furniture.wordy;

import burrow.core.chamber.Chamber;
import burrow.core.config.Config;
import burrow.core.furniture.BurrowFurniture;
import burrow.core.furniture.Furniture;
import burrow.furniture.keyvalue.KeyValueFurniture;
import burrow.furniture.time.TimeFurniture;
import org.springframework.lang.NonNull;

@BurrowFurniture(
    simpleName = "Wordy",
    description = "Learn and review vocabulary with Wordy!",
    dependencies = {
        KeyValueFurniture.class,
        TimeFurniture.class
    }
)
public class WordyFurniture extends Furniture {
    public WordyFurniture(@NonNull final Chamber chamber) {
        super(chamber);
    }

    @Override
    public void init() {
    }

    @Override
    public void initConfig(@NonNull final Config config) {
        config.set(KeyValueFurniture.ConfigKey.KV_KEY_NAME, EntryKey.WORD);
        config.set(KeyValueFurniture.ConfigKey.KV_VALUE_NAME, EntryKey.TRANSLATION);
    }

    public @interface EntryKey {
        String WORD = "word";
        String TRANSLATION = "translation";
    }
}
