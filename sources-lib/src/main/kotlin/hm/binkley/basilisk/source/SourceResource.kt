package hm.binkley.basilisk.source

import hm.binkley.basilisk.location.LocationResource

data class SourceResource(
        val code: String,
        val name: String,
        val locations: List<LocationResource>) {
    constructor(source: SourceDetails) : this(source.code, source.name,
            source.locations.map { LocationResource(it) })
}
