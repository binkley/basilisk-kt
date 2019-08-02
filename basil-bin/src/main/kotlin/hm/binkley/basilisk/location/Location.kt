package hm.binkley.basilisk.location

import hm.binkley.basilisk.db.findOne
import io.micronaut.context.event.ApplicationEvent
import io.micronaut.context.event.ApplicationEventPublisher
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable
import java.util.*
import javax.inject.Singleton

@Singleton
class Locations(private val publisher: ApplicationEventPublisher) {
    fun location(code: String) = LocationRecord.findOne {
        LocationRepository.code eq code
    }?.let {
        location(it)
    }

    fun location(record: LocationRecord) = Location(record, this)

    fun create(name: String, code: String) = Location(LocationRecord.new {
        this.name = name
        this.code = code
    }, this).save()

    fun notifySaved(location: Location) {
        publisher.publishEvent(LocationSavedEvent(location))
    }

    override fun toString() =
            "${super.toString()}{publisher=$publisher}"
}

object LocationRepository : IntIdTable("LOCATION") {
    val name = text("name")
    val code = text("code")
}

interface LocationDetails {
    val name: String
    val code: String
}

data class LocationSavedEvent(val after: Location) : ApplicationEvent(after)

class Location(
        private val record: LocationRecord,
        private val factory: Locations)
    : LocationDetails by record {

    fun save(): Location {
        record.flush()
        factory.notifySaved(this)
        return this
    }

    override fun toString() = "${super.toString()}{record=$record}"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Location

        return record == other.record
    }

    override fun hashCode() = record.hashCode()
}

class LocationRecord(id: EntityID<Int>) : IntEntity(id),
        LocationDetails {
    companion object : IntEntityClass<LocationRecord>(LocationRepository)

    override var name by LocationRepository.name
    override var code by LocationRepository.code

    override fun toString() =
            "${super.toString()}{id=$id, name=$name, code=$code}"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as LocationRecord
        return name == other.name
                && code == other.code
    }

    override fun hashCode() = Objects.hash(name, code)
}
