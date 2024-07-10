package burrow.chain;

import burrow.chain.event.Event;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Reactor<C extends Context> {
    private final Map<Class<? extends Event>, List<Listener<C, ? extends Event>>>
        eventListenerStore = new HashMap<>();

    @NotNull
    private List<Listener<C, ? extends Event>> getEventListenerList(
        @NotNull final Class<? extends Event> eventClass) {
        return eventListenerStore.computeIfAbsent(eventClass, (k) -> new ArrayList<>());
    }

    public <E extends Event> void on(
        @NotNull final Class<E> eventClass,
        @NotNull final Listener<C, E> listener
    ) {
        getEventListenerList(eventClass).add(listener);
    }

    public void trigger(@NotNull final Event event, @NotNull final C context) {
        getEventListenerList(event.getClass()).forEach(listener -> listener.accept(context, event));
    }
}
