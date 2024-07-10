package burrow.chain;

import burrow.chain.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.Queue;

public class Context extends AbstractContext {
    protected static <T> ContextHook<T> hook(@NotNull final String key) {
        return new ContextHook<>(key);
    }

    @Nullable
    public Queue<Event> getEventQueue() {
        return Hook.eventQueue.get(this);
    }

    public void setEventQueue(@Nullable final Queue<Event> eventQueue) {
        Hook.eventQueue.set(this, eventQueue);
    }

    @Nullable
    public Boolean getTerminationFlag() {
        return Hook.terminationFlag.get(this);
    }

    public void setTerminationFlag(@Nullable final Boolean terminationFlag) {
        Hook.terminationFlag.set(this, terminationFlag);
    }

    public void trigger(@NotNull final Event event) {
        final var eventQueue = Context.Hook.eventQueue.computeIfAbsent(this, LinkedList::new);
        eventQueue.add(event);
    }

    public @interface Hook {
        ContextHook<Queue<Event>> eventQueue = hook("eventQueue");
        ContextHook<Boolean> terminationFlag = hook("terminationFlag");
    }
}
