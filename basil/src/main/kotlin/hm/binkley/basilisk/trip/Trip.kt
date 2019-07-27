package hm.binkley.basilisk.trip

import hm.binkley.basilisk.chef.ChefRecord
import hm.binkley.basilisk.chef.ChefRepository
import hm.binkley.basilisk.leg.LegRecord
import hm.binkley.basilisk.leg.LegRepository
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable

object TripRepository : IntIdTable("TRIP") {
    val name = text("name")
    val chef = reference("chef_id", ChefRepository)
}

class TripRecord(id: EntityID<Int>) : IntEntity(id),
        Span<LegRecord> {
    companion object : IntEntityClass<TripRecord>(
            TripRepository)

    var name by TripRepository.name
    var chef by ChefRecord referencedOn TripRepository.chef
    private val _legs by LegRecord referrersOn LegRepository.trip
    // TODO: What is right here?  Memoized or live?
    val legs: Iterable<LegRecord> by lazy {
        sort(_legs)
    }
    override val start
        get() = legs.first()
    override val end
        get() = legs.last()
}
