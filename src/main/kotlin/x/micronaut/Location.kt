package x.micronaut

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable

class Location(private val record: LocationRecord) {
    val name
        get() = record.name
}

object LocationRepository : IntIdTable("LOCATION") {
    val name = text("name")
}

class LocationRecord(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<LocationRecord>(LocationRepository)

    var name by LocationRepository.name
}
