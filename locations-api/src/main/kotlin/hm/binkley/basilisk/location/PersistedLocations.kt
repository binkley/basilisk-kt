package hm.binkley.basilisk.location

import hm.binkley.basilisk.db.CodeEntity
import hm.binkley.basilisk.db.CodeEntityClass
import hm.binkley.basilisk.db.CodeIdTable
import hm.binkley.basilisk.db.findOne
import hm.binkley.basilisk.domain.notifyChanged
import io.micronaut.context.event.ApplicationEventPublisher
import org.jetbrains.exposed.dao.EntityID
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

    internal fun notifyChanged(
            before: LocationResource?, after: LocationRecord?) =
            notifyChanged(before, after?.let {
                LocationResource(it)
            }, publisher, ::LocationSavedEvent)
}

class PersistedLocation internal constructor(
        internal val record: LocationRecord,
        private val factory: PersistedLocations)
    : Location,
        LocationDetails by record {
    private var snapshot: LocationResource? = LocationResource(this)

    /** @throws IllegalStateException if this location has been deleted */
    override fun update(block: MutableLocation.() -> Unit) =
            update(checkNotNull(snapshot), block)

    internal inline fun update(
            snapshot: LocationResource?,
            block: MutablePersistedLocation.() -> Unit) = apply {
        MutablePersistedLocation(snapshot, { newSnapshot ->
            this.snapshot = newSnapshot
        }, record, factory).block()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as PersistedLocation
        return snapshot == snapshot
                && record == other.record
    }

    override fun hashCode() = Objects.hash(snapshot, record)

    override fun toString() =
            "${super.toString()}{snapshot=$snapshot, record=$record}"
}

class MutablePersistedLocation internal constructor(
        private val snapshot: LocationResource?,
        private val setSnapshot: (LocationResource?) -> Unit,
        private val record: LocationRecord,
        private val factory: PersistedLocations)
    : MutableLocation,
        MutableLocationDetails by record {
    override fun save() = apply {
        record.flush()
        factory.notifyChanged(snapshot, record)
        setSnapshot(LocationResource(record))
    }

    override fun delete() {
        record.delete()
        factory.notifyChanged(snapshot, null)
        setSnapshot(null)
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

object LocationRepository : CodeIdTable("LOCATION") {
    val code = text("code")
    val name = text("name")
}

class LocationRecord(id: EntityID<String>) : CodeEntity(id),
        LocationDetails,
        MutableLocationDetails {
    companion object : CodeEntityClass<LocationRecord>(LocationRepository)

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
