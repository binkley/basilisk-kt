package hm.binkley.basilisk

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable

object TripRepository : IntIdTable("TRIP") {
    val name = text("name")
    val chef = reference("chef_id", ChefRepository)
}

class TripRecord(id: EntityID<Int>) : IntEntity(id), Span<LegRecord> {
    companion object : IntEntityClass<TripRecord>(TripRepository)

    var name by TripRepository.name
    var chef by ChefRecord referencedOn TripRepository.chef
    private val _legs by LegRecord referrersOn LegRepository.trip
    val legs: Iterable<LegRecord>
        get() = sort(_legs)
    override val start = legs.first()
    override val end = legs.last()
}
