package hm.binkley.basilisk.location

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
    var code: String
    var name: String
}

interface Location : LocationDetails {
    fun update(block: MutableLocation.() -> Unit): Location
}

interface MutableLocation : MutableLocationDetails {
    fun save(): MutableLocation

    fun delete()
}
