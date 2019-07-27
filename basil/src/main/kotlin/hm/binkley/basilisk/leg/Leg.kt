package hm.binkley.basilisk.leg

import hm.binkley.basilisk.location.LocationRecord
import hm.binkley.basilisk.location.LocationRepository
import hm.binkley.basilisk.trip.Span
import hm.binkley.basilisk.trip.TripRecord
import hm.binkley.basilisk.trip.TripRepository
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable

object LegRepository : IntIdTable("LEG") {
    val trip = reference("trip_id", TripRepository)
    val start = reference("start_location_id",
            LocationRepository)
    val startAt = datetime("start_at")
    val end = reference("end_location_id",
            LocationRepository)
    val endAt = datetime("end_at")
}

class LegRecord(id: EntityID<Int>) : IntEntity(id),
        Span<LocationRecord> {
    companion object : IntEntityClass<LegRecord>(
            LegRepository)

    var trip by TripRecord referencedOn LegRepository.trip
    override var start by LocationRecord referencedOn LegRepository.start
    var startAt by LegRepository.startAt
    override var end by LocationRecord referencedOn LegRepository.end
    var endAt by LegRepository.endAt
}
