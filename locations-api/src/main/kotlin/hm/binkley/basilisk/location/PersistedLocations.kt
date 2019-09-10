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
import kotlin.reflect.KMutableProperty0

@Singleton
class PersistedLocations(private val publisher: ApplicationEventPublisher)
    : Locations {
    override fun all() = LocationRecord.all().map {
        PersistedLocation(LocationResource(it), it, this)
    }

    override fun byCode(code: String) = LocationRecord.findOne {
        LocationRepository.code eq code
    }?.let {
        PersistedLocation(LocationResource(it), it, this)
    }

    override fun new(location: LocationResource) = LocationRecord.new {
        this.code = location.code
        this.name = location.name
    }.let {
        PersistedLocation(null, it, this)
    }

    internal fun notifyChanged(event: LocationChangedEvent) =
            notifyChanged(event.before, event.after,
                    publisher, ::LocationChangedEvent)

    // TODO: TEMP code until remodelling passes to other classes

    fun from(record: LocationRecord) =
            PersistedLocation(LocationResource(record), record, this)

    fun toRecord(location: PersistedLocation) =
            location.record
}

class PersistedLocation internal constructor(
        private var snapshot: LocationResource?,
        internal val record: LocationRecord, // TODO: private
        private val factory: PersistedLocations)
    : Location,
        LocationDetails by record {
    override fun update(block: MutableLocation.() -> Unit) = apply {
        PersistedMutableLocation(::snapshot, record, factory).block()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as PersistedLocation
        return snapshot == other.snapshot
                && record == other.record
    }

    override fun hashCode() = Objects.hash(snapshot, record)

    override fun toString() =
            "${super.toString()}{snapshot=$snapshot, record=$record}"
}

class PersistedMutableLocation internal constructor(
        private val snapshot: KMutableProperty0<LocationResource?>,
        private val record: LocationRecord,
        private val factory: PersistedLocations)
    : MutableLocation,
        MutableLocationDetails by record {
    override fun save() = apply {
        record.flush()
        factory.notifyChanged(
                LocationChangedEvent(snapshot.get(),
                        LocationResource(record)))
        snapshot.set(LocationResource(record))
    }

    override fun delete() {
        record.delete()
        factory.notifyChanged(
                LocationChangedEvent(snapshot.get(), null))
        snapshot.set(null)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as PersistedMutableLocation
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
