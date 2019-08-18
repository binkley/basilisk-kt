package hm.binkley.basilisk.location

data class LocationResource(val name: String, val code: String) {
    constructor(location: PersistedLocation) : this(location.name,
            location.code)
}
