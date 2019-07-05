package hm.binkley.basilisk

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable

class Trip(private val record: TripRecord) {
    val name
        get() = record.name
    val chef
        get() = record.chef
}

object TripRepository : IntIdTable("TRIP") {
    val name = text("name")
    val chef = IngredientRepository.reference("chef_id", ChefRepository)
}

class TripRecord(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<TripRecord>(TripRepository)

    var name by TripRepository.name
    var chef by ChefRecord referencedOn IngredientRepository.chef
}
