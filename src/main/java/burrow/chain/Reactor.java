package burrow.chain;

import burrow.chain.event.Event;
import org.springframework.lang.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Reactor<C extends Context> {
    private final Map<Class<? extends Event>, List<Listener<C, ? extends Event>>>
        eventListenerStore = new HashMap<>();

    @NonNull
    private List<Listener<C, ? extends Event>> getEventListenerList(
        @NonNull final Class<? extends Event> eventClass) {
        return eventListenerStore.computeIfAbsent(eventClass, (k) -> new ArrayList<>());
    }

    public <E extends Event> void on(
        @NonNull final Class<E> eventClass,
        @NonNull final Listener<C, E> listener
    ) {
        getEventListenerList(eventClass).add(listener);
    }

    public void trigger(@NonNull final Event event, @NonNull final C context) {
        getEventListenerList(event.getClass()).forEach(listener -> listener.accept(context, event));
    }
}
