package hm.binkley.basilisk.domain

import io.micronaut.context.event.ApplicationEvent
import io.micronaut.context.event.ApplicationEventPublisher

fun <Details, Event : ApplicationEvent> notifyChanged(
        before: Details?, after: Details?,
        publisher: ApplicationEventPublisher,
        event: (Details?, Details?) -> Event) {
    // Only publish if changed, not if unchanged
    if (before == after) return
    publisher.publishEvent(event(before, after))
}
