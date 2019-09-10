package hm.binkley.basilisk.source

import hm.binkley.basilisk.location.LocationResource

data class SourceResource(
        override val code: String,
        override val name: String,
        override val locations: List<LocationResource>) : SourceDetails {
    constructor(source: SourceDetails) : this(source.code, source.name,
            source.locations.map { LocationResource(it) })
}
