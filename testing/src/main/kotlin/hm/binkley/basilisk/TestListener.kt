package hm.binkley.basilisk

import io.micronaut.context.event.ApplicationEvent
import io.micronaut.context.event.ApplicationEventListener
import javax.inject.Singleton

@Singleton
class TestListener<E : ApplicationEvent> : ApplicationEventListener<E> {
    private val _received = mutableListOf<E>()

    val received: List<E> = _received

    fun reset() = _received.clear()

    override fun onApplicationEvent(event: E) {
        _received.add(event)
    }
}
