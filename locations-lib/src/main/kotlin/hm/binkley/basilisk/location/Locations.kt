package hm.binkley.basilisk.location

import io.micronaut.context.event.ApplicationEvent

interface Locations {
    fun all(): Iterable<Location>

    fun byCode(code: String): Location?

    fun new(location: LocationResource): Location
}

interface LocationDetails {
    val code: String
    val name: String
}

interface MutableLocationDetails {
    val code: String
    var name: String
}

interface Location : LocationDetails {
    fun update(block: MutableLocation.() -> Unit): Location
}

interface MutableLocation : MutableLocationDetails {
    fun save(): MutableLocation

    fun delete()
}

data class LocationSavedEvent(
        val before: LocationResource?,
        val after: Location?) : ApplicationEvent(after ?: before)
