package burrow.kernel.event

import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

class EventBus {
    private val eventHandlers =
        ConcurrentHashMap<KClass<out Event>, MutableSet<EventHandler<*>>>()

    fun <E : Event> subscribe(
        eventClass: KClass<E>,
        handler: EventHandler<E>
    ) {
        eventHandlers.computeIfAbsent(eventClass) { mutableSetOf() }
            .add(handler)
    }

    fun <E : Event> unsubscribe(
        eventClass: KClass<E>,
        handler: EventHandler<E>
    ) {
        eventHandlers.computeIfAbsent(eventClass) { mutableSetOf() }
            .remove(handler)
    }

    fun <E : Event> post(event: E) {
        eventHandlers[event::class]?.forEach { handler ->
            @Suppress("UNCHECKED_CAST")
            (handler as EventHandler<E>)(event)
        }
    }
}