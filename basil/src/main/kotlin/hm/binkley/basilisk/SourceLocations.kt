package hm.binkley.basilisk

import hm.binkley.basilisk.location.LocationRepository
import hm.binkley.basilisk.source.SourceRepository
import org.jetbrains.exposed.sql.Table

object SourceLocationsRepository : Table("SOURCE_LOCATION") {
    val sourceRef = reference("source_id",
            SourceRepository).primaryKey(0)
    val location = reference("location_id",
            LocationRepository).primaryKey(1)
}
