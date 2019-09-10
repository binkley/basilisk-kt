package hm.binkley.basilisk.domain

import io.micronaut.context.event.ApplicationEvent
import io.micronaut.context.event.ApplicationEventPublisher

fun <Resource, Event : ApplicationEvent> notifyChanged(
        before: Resource?, after: Resource?,
        publisher: ApplicationEventPublisher,
        event: (Resource?, Resource?) -> Event) {
    // Only publish if changed, not if unchanged
    if (before == after) return
    publisher.publishEvent(event(before, after))
}
