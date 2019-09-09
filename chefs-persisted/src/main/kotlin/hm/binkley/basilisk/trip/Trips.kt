package hm.binkley.basilisk.trip

import hm.binkley.basilisk.chef.ChefRecord
import hm.binkley.basilisk.chef.ChefRepository
import hm.binkley.basilisk.leg.LegRecord
import hm.binkley.basilisk.leg.LegRepository.trip
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable
import java.util.*

object TripRepository : IntIdTable("TRIP") {
    val name = text("name")
    val chef = reference("chef_code", ChefRepository)
}

class TripRecord(id: EntityID<Int>) : IntEntity(id),
        Span<LegRecord> {
    companion object : IntEntityClass<TripRecord>(TripRepository)

    var name by TripRepository.name
    var chef by ChefRecord referencedOn TripRepository.chef
    private val _legs by LegRecord referrersOn trip
    // TODO: What is right here?  Memoized or live?
    val legs: Iterable<LegRecord> by lazy {
        // TODO: Bleh.  Use Kotlin stdlib instead of everything by hand
        // TODO: Also, is this querying the DB -- each time?
        val (sorted, _) = sort(_legs)
        val tripIter = sorted.iterator()
        check(tripIter.hasNext()) { "BUG: No sorted spans" }
        val legs = tripIter.next()
        val spansIter = legs.iterator()
        check(spansIter.hasNext()) { "BUG: No first span after sorting" }
        spansIter.next()
        check(spansIter.hasNext()) {
            "Disconnected legs in trip: ${_legs.toList()}"
        }
        legs
    }
    override val start
        get() = legs.first()
    override val end
        get() = legs.last()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as TripRecord
        return name == other.name
                && chef == other.chef
                && legs == other.legs
    }

    override fun hashCode() = Objects.hash(name, chef, start, legs)

    override fun toString() =
            "${super.toString()}{id=$id, name=$name, chef=$chef, start=$start, end=$end, legs=$legs}"
}
