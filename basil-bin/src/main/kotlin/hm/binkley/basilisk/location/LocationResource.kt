package hm.binkley.basilisk.location

data class LocationResource(val name: String, val code: String) {
    constructor(location: Location) : this(location.name, location.code)
}
