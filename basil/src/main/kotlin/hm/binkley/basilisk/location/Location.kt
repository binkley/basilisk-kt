package hm.binkley.basilisk.location

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable
import java.util.*

object LocationRepository : IntIdTable("LOCATION") {
    val name = text("name")
    val code = text("code")
}

class LocationRecord(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<LocationRecord>(
            LocationRepository)

    var name by LocationRepository.name
    var code by LocationRepository.code

    override fun toString(): String {
        return "${super.toString()}{id=$id, name=$name, code=$code}"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as LocationRecord
        return name == other.name
                && code == other.code
    }

    override fun hashCode(): Int {
        return Objects.hash(name, code)
    }
}
