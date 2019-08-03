package hm.binkley.basilisk.source

import hm.binkley.basilisk.location.LocationRepository
import org.jetbrains.exposed.sql.Table

object SourceLocationsRepository : Table("SOURCE_LOCATION") {
    // TODO: The hierarchy of `Table` defines a visible "source" field
    val sourceRef = reference("source_id", SourceRepository)
            .primaryKey(0)
    val location = reference("location_id", LocationRepository)
            .primaryKey(1)
}
