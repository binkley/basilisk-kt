package hm.binkley.basilisk.location

interface Locations {
    fun all(): Iterable<PersistedLocation>

    fun byCode(code: String): PersistedLocation?

    fun new(location: LocationResource): PersistedLocation
}

interface LocationDetails {
    val name: String
    val code: String
}

interface MutableLocationDetails {
    var name: String
    var code: String
}

interface Location : LocationDetails {
    fun update(block: MutablePersistedLocation.() -> Unit): PersistedLocation
}

interface MutableLocation : MutableLocationDetails {
    fun save(): MutablePersistedLocation

    fun delete()
}
