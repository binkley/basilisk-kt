package hm.binkley.basilisk

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable

object SourceRepository : IntIdTable("SOURCE") {
    val name = text("name")
}

class SourceRecord(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<SourceRecord>(SourceRepository)

    var name by SourceRepository.name
}
