package hm.binkley.basilisk.domain

import io.micronaut.context.event.ApplicationEvent
import io.micronaut.context.event.ApplicationEventPublisher

fun <Resource, Domain, Event : ApplicationEvent> notifySaved(
        before: Resource?, after: Domain?,
        publisher: ApplicationEventPublisher,
        resource: (Domain) -> Resource,
        event: (Resource?, Domain?) -> Event) {
    // Only publish if changed, not if unchanged
    val afterSnapshot = after?.let { resource(after) }
    if (before != afterSnapshot) {
        publisher.publishEvent(event(before, after))
    }
}
