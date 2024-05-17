package me.jameschan.burrow.hoard;

import me.jameschan.burrow.chamber.Chamber;
import me.jameschan.burrow.chamber.ChamberBased;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class Hoard extends ChamberBased {
    public static final String KEY_ID = "id";

    private final Map<Integer, Entry> byId = new HashMap<>();

    private Integer maxId = 0;

    public Hoard(final Chamber chamber) {
        super(chamber);
    }

    public Entry getById(final int id) {
        return Optional.ofNullable(byId.get(id)).orElseThrow(() -> new EntryNotFoundException(id));
    }

    public Entry create(final Map<String, String> properties) {
        final var id = ++maxId;
        final var entry = new Entry(id);

        this.byId.put(id, entry);
        properties.forEach(entry::set);

        return entry;
    }

    public void register(final Map<String, String> entryObject) {
        final var id = Integer.parseInt(entryObject.get(KEY_ID));
        if (byId.containsKey(id)) {
            throw new DuplicateIdException(id);
        }

        final var entry = new Entry(id);
        byId.put(id, entry);
        maxId = Math.max(maxId, id);
    }

    public Entry delete(final int id) {
        final var entry = getById(id);
        byId.remove(id);

        return entry;
    }
}