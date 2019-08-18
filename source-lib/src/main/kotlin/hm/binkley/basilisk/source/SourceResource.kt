package hm.binkley.basilisk.source

import hm.binkley.basilisk.location.LocationResource

data class SourceResource(
        val name: String,
        val code: String,
        val locations: List<LocationResource>) {
    constructor(source: PersistedSource) : this(source.name, source.code,
            source.locations.map { LocationResource(it) })
}
