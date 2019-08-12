package hm.binkley.basilisk.domain

import io.micronaut.context.event.ApplicationEvent
import io.micronaut.context.event.ApplicationEventPublisher
import org.jetbrains.exposed.dao.IntEntity

fun <Resource,
        Record : IntEntity,
        Domain,
        Event : ApplicationEvent> notifySaved(
        before: Resource?,
        after: Record?,
        resource: (Domain) -> Resource,
        from: (Record) -> Domain,
        event: (Resource?, Domain?) -> Event,
        publisher: ApplicationEventPublisher) {
    // TODO: Restructure how domain->mutable works, to avoid using records

    // Only publish if changed, not if unchanged
    val afterSnapshot = after?.let { resource(from(it)) }
    if (before != afterSnapshot)
        publisher.publishEvent(event(before, after?.let { from(it) }))
}
