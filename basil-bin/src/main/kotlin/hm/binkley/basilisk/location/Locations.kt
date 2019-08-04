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
    fun location(code: String): Location? {
        val record = LocationRecord.findOne {
            LocationRepository.code eq code
        }

        return record?.let { location(it) }
    }

    fun new(name: String, code: String) = Location(LocationRecord.new {
        this.name = name
        this.code = code
    }, this).update(null) {
        save()
    }

    fun all() = LocationRecord.all().map {
        location(it)
    }

    /** For implementors of other record types having a reference. */
    fun location(record: LocationRecord) = Location(record, this)

    internal fun notifySaved(
            before: LocationResource?,
            after: MutableLocation?) {
        publisher.publishEvent(LocationSavedEvent(before, after))
    }
}

interface LocationDetails {
    val name: String
    val code: String
}

interface MutableLocationDetails {
    var name: String
    var code: String
}

data class LocationSavedEvent(
        val before: LocationResource?,
        val after: MutableLocation?) : ApplicationEvent(after ?: before)

class Location internal constructor(
        private val record: LocationRecord,
        private val factory: Locations)
    : LocationDetails by record {
    fun update(block: MutableLocation.() -> Unit) =
            update(LocationResource(this), block)

    internal inline fun update(
            snapshot: LocationResource?,
            block: MutableLocation.() -> Unit) = apply {
        mutable(snapshot).block()
    }

    internal fun mutable(snapshot: LocationResource?) =
            MutableLocation(snapshot, record, factory)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Location
        return record == other.record
    }

    override fun hashCode() = record.hashCode()

    override fun toString() = "${super.toString()}{record=$record}"
}

class MutableLocation internal constructor(
        private val snapshot: LocationResource?,
        private val record: LocationRecord,
        private val factory: Locations) : MutableLocationDetails by record {
    fun save() = apply {
        record.flush() // TODO: Aggressively flush, or wait for txn to end?
        factory.notifySaved(snapshot, this)
    }

    fun delete() {
        record.delete() // TODO: Detect if edited, and not saved, then deleted
        factory.notifySaved(snapshot, null)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as MutableLocation
        return snapshot == other.snapshot
                && record == other.record
    }

    override fun hashCode() = Objects.hash(snapshot, record)

    override fun toString() =
            "${super.toString()}{snapshot=$snapshot, record=$record}"
}

object LocationRepository : IntIdTable("LOCATION") {
    val name = text("name")
    val code = text("code")
}

class LocationRecord(id: EntityID<Int>) : IntEntity(id),
        LocationDetails,
        MutableLocationDetails {
    companion object : IntEntityClass<LocationRecord>(LocationRepository)

    override var name by LocationRepository.name
    override var code by LocationRepository.code

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as LocationRecord
        return name == other.name
                && code == other.code
    }

    override fun hashCode() = Objects.hash(name, code)

    override fun toString() =
            "${super.toString()}{id=$id, name=$name, code=$code}"
}