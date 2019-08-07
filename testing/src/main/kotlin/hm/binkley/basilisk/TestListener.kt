package hm.binkley.basilisk

import ch.tutteli.atrium.creating.ReportingAssertionPlant
import ch.tutteli.atrium.verbs.expect
import io.micronaut.context.event.ApplicationEvent
import io.micronaut.context.event.ApplicationEventListener
import javax.inject.Singleton

@Singleton
class TestListener<E : ApplicationEvent> : ApplicationEventListener<E> {
    private val received = mutableListOf<E>()

    fun reset() {
        received.clear()
    }

    override fun onApplicationEvent(event: E) {
        received.add(event)
    }

    val expect: ReportingAssertionPlant<List<E>>
        get() = expect(received.toList()).also {
            reset()
        }
}
