package burrow.kernel.event

abstract class Event

fun interface EventHandler<in E : Event> {
    operator fun invoke(event: E)
}