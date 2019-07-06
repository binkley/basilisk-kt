package hm.binkley.basilisk

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable

object LegRepository : IntIdTable("LEG") {
    val trip = reference("trip_id", TripRepository)
    val start = reference("start_id", LocationRepository)
    val end = reference("end_id", LocationRepository)
}

class LegRecord(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<LegRecord>(LegRepository)

    var trip by TripRecord referencedOn LegRepository.trip
    var start by LocationRecord referencedOn LegRepository.start
    var end by LocationRecord referencedOn LegRepository.end
}
