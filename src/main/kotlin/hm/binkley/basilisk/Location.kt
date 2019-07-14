package hm.binkley.basilisk

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable
import java.util.*

object LocationRepository : IntIdTable("LOCATION") {
    val name = text("name")
}

class LocationRecord(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<LocationRecord>(LocationRepository)

    var name by LocationRepository.name

    override fun toString(): String {
        return "${super.toString()}{id=$id, name=$name}"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        val that = other as LocationRecord
        return id == that.id && name == that.name
    }

    override fun hashCode(): Int {
        return Objects.hash(id, name)
    }
}
