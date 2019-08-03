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
import java.util.*

object LegRepository : IntIdTable("LEG") {
    val trip = reference("trip_id", TripRepository)
    val start = reference("start_location_id", LocationRepository)
    val startAt = datetime("start_at")
    val end = reference("end_location_id", LocationRepository)
    val endAt = datetime("end_at")
}

class LegRecord(id: EntityID<Int>) : IntEntity(id),
        Span<LocationRecord> {
    companion object : IntEntityClass<LegRecord>(LegRepository)

    var trip by TripRecord referencedOn LegRepository.trip
    override var start by LocationRecord referencedOn LegRepository.start
    var startAt by LegRepository.startAt
    override var end by LocationRecord referencedOn LegRepository.end
    var endAt by LegRepository.endAt

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as LegRecord
        return trip.id == other.trip.id
                && start.id == other.start.id
                && startAt == other.startAt
                && end.id == other.end.id
                && endAt == other.endAt
    }

    override fun hashCode() =
            Objects.hash(trip.id, start.id, startAt, end.id, endAt)

    override fun toString() =
            "${super.toString()}{id=$id, trip_id=${trip.id}, start=${start.code}, startAt=$startAt, end=${end.code}, endAt=$endAt}"
}
