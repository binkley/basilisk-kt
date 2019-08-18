package hm.binkley.basilisk.source

import hm.binkley.basilisk.db.asList
import hm.binkley.basilisk.db.findOne
import hm.binkley.basilisk.domain.notifySaved
import hm.binkley.basilisk.location.LocationRecord
import hm.binkley.basilisk.location.PersistedLocation
import hm.binkley.basilisk.location.PersistedLocations
import io.micronaut.context.event.ApplicationEvent
import io.micronaut.context.event.ApplicationEventPublisher
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.sql.SizedCollection
import org.jetbrains.exposed.sql.SizedIterable
import org.jetbrains.exposed.sql.emptySized
import org.jetbrains.exposed.sql.mapLazy
import java.util.*
import javax.inject.Singleton

@Singleton
class PersistedSources(
        private val locations: PersistedLocations,
        private val publisher: ApplicationEventPublisher) {
    fun byCode(code: String) = SourceRecord.findOne {
        SourceRepository.code eq code
    }?.let {
        from(it)
    }

    fun new(name: String, code: String,
            locations: MutableList<PersistedLocation> = mutableListOf()) =
            from(SourceRecord.new {
                this.name = name
                this.code = code
            }).update(null) {
                // Exposed wants the record complete before adding relationships
                this.locations = locations
                save()
            }

    /** For implementors of other record types having a reference. */
    fun from(record: SourceRecord) = PersistedSource(record, this)

    /** For implementors of other record types having a reference. */
    fun toRecord(source: PersistedSource) = source.record

    internal fun notifySaved(before: SourceResource?, after: SourceRecord?) =
            notifySaved(before, after?.let { from(it) }, publisher,
                    ::SourceResource, ::SourceSavedEvent)

    internal fun locationFrom(locationRecord: LocationRecord) =
            locations.from(locationRecord)

    internal fun toRecord(location: PersistedLocation) =
            locations.toRecord(location)
}

interface SourceDetails {
    val name: String
    val code: String
}

interface MutableSourceDetails {
    var name: String
    var code: String
}

data class SourceSavedEvent(
        val before: SourceResource?,
        val after: PersistedSource?) : ApplicationEvent(after ?: before)

class PersistedSource internal constructor(
        internal val record: SourceRecord,
        private val factory: PersistedSources)
    : SourceDetails by record {
    val locations: SizedIterable<PersistedLocation>
        get() = record.locations.notForUpdate().mapLazy {
            factory.locationFrom(it)
        }

    fun update(block: MutableSource.() -> Unit) =
            update(SourceResource(this), block)

    internal inline fun update(
            snapshot: SourceResource?,
            block: MutableSource.() -> Unit) = apply {
        MutableSource(snapshot, record, factory).block()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PersistedSource

        return record == other.record
    }

    override fun hashCode() = record.hashCode()

    override fun toString() = "${super.toString()}{record=$record}"
}

class MutableSource internal constructor(
        private val snapshot: SourceResource?,
        private val record: SourceRecord,
        private val factory: PersistedSources) : MutableSourceDetails by record {
    var locations: MutableList<PersistedLocation>
        get() {
            val update = record.locations.forUpdate().mapLazy {
                factory.locationFrom(it)
            }.asList { update ->
                locations = update
            }
            locations = update // Glue changes of list back to record
            return update
        }
        set(update) {
            record.locations = SizedCollection(update.map {
                factory.toRecord(it)
            })
        }

    fun save() = apply {
        record.flush()
        factory.notifySaved(snapshot, record)
    }

    fun delete() {
        record.delete()
        factory.notifySaved(snapshot, null)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as MutableSource
        return snapshot == other.snapshot
                && record == other.record
    }

    override fun hashCode() = Objects.hash(snapshot, record)

    override fun toString() =
            "${super.toString()}{snapshot=$snapshot, record=$record}"
}

object SourceRepository : IntIdTable("SOURCE") {
    val name = text("name")
    val code = text("code")
}

class SourceRecord(id: EntityID<Int>) : IntEntity(id),
        SourceDetails,
        MutableSourceDetails {
    companion object : IntEntityClass<SourceRecord>(SourceRepository)

    override var name by SourceRepository.name
    override var code by SourceRepository.code
    var locations by LocationRecord via SourceLocationsRepository

    override fun delete() {
        locations = emptySized()
        super.delete()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as SourceRecord
        return name == other.name
                && code == other.code
                && locations == other.locations
    }

    override fun hashCode() = Objects.hash(name, code, locations)

    override fun toString() =
            "${super.toString()}{id=$id, name=$name, code=$code, locations=$locations}"
}
