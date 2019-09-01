package hm.binkley.basilisk.location

import hm.binkley.basilisk.db.findOne
import hm.binkley.basilisk.domain.notifySaved
import io.micronaut.context.event.ApplicationEvent
import io.micronaut.context.event.ApplicationEventPublisher
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable
import java.util.*
import javax.inject.Singleton

@Singleton
class PersistedLocations(private val publisher: ApplicationEventPublisher)
    : Locations {
    override fun byCode(code: String) = LocationRecord.findOne {
        LocationRepository.code eq code
    }?.let {
        from(it)
    }

    override fun all() = LocationRecord.all().map {
        from(it)
    }

    override fun new(location: LocationResource) = from(LocationRecord.new {
        this.code = location.code
        this.name = location.name
    }).update(null) {
        save()
    }

    /** For implementors of other record types having a reference. */
    fun from(record: LocationRecord) = PersistedLocation(record, this)

    /** For implementors of other record types having a reference. */
    fun toRecord(location: PersistedLocation) = location.record

    internal fun notifySaved(
            before: LocationResource?, after: LocationRecord?) =
            notifySaved(before, after?.let { from(it) }, publisher,
                    ::LocationResource, ::LocationSavedEvent)
}

data class LocationSavedEvent(
        val before: LocationResource?,
        val after: PersistedLocation?) : ApplicationEvent(after ?: before)

class PersistedLocation internal constructor(
        internal val record: LocationRecord,
        private val factory: PersistedLocations)
    : Location,
        LocationDetails by record {
    override fun update(block: MutableLocation.() -> Unit) =
            update(LocationResource(this), block)

    internal inline fun update(
            snapshot: LocationResource?,
            block: MutablePersistedLocation.() -> Unit) = apply {
        MutablePersistedLocation(snapshot, record, factory).block()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as PersistedLocation
        return record == other.record
    }

    override fun hashCode() = record.hashCode()

    override fun toString() = "${super.toString()}{record=$record}"
}

class MutablePersistedLocation internal constructor(
        private val snapshot: LocationResource?,
        private val record: LocationRecord,
        private val factory: PersistedLocations)
    : MutableLocation,
        MutableLocationDetails by record {
    override fun save() = apply {
        record.flush()
        factory.notifySaved(snapshot, record)
    }

    override fun delete() {
        record.delete()
        factory.notifySaved(snapshot, null)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as MutablePersistedLocation
        return snapshot == other.snapshot
                && record == other.record
    }

    override fun hashCode() = Objects.hash(snapshot, record)

    override fun toString() =
            "${super.toString()}{snapshot=$snapshot, record=$record}"
}

object LocationRepository : IntIdTable("LOCATION") {
    val code = text("code")
    val name = text("name")
}

class LocationRecord(id: EntityID<Int>) : IntEntity(id),
        LocationDetails,
        MutableLocationDetails {
    companion object : IntEntityClass<LocationRecord>(LocationRepository)

    override var code by LocationRepository.code
    override var name by LocationRepository.name

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as LocationRecord
        return code == other.code
                && name == other.name
    }

    override fun hashCode() = Objects.hash(code, name)

    override fun toString() =
            "${super.toString()}{id=$id, code=$code, name=$name}"
}
