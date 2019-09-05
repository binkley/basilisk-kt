package hm.binkley.basilisk.source

import hm.binkley.basilisk.db.CodeEntity
import hm.binkley.basilisk.db.CodeEntityClass
import hm.binkley.basilisk.db.CodeIdTable
import hm.binkley.basilisk.db.asList
import hm.binkley.basilisk.db.findOne
import hm.binkley.basilisk.domain.notifySaved
import hm.binkley.basilisk.location.LocationRecord
import hm.binkley.basilisk.location.PersistedLocation
import hm.binkley.basilisk.location.PersistedLocations
import io.micronaut.context.event.ApplicationEvent
import io.micronaut.context.event.ApplicationEventPublisher
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.SizedCollection
import org.jetbrains.exposed.sql.SizedIterable
import org.jetbrains.exposed.sql.emptySized
import org.jetbrains.exposed.sql.mapLazy
import java.util.*
import javax.inject.Singleton

@Singleton
class Sources(
        private val locations: PersistedLocations,
        private val publisher: ApplicationEventPublisher) {
    fun byCode(code: String) = SourceRecord.findOne {
        SourceRepository.code eq code
    }?.let {
        from(it)
    }

    fun new(code: String, name: String,
            locations: MutableList<PersistedLocation> = mutableListOf()) =
            from(SourceRecord.new {
                this.code = code
                this.name = name
            }).update(null) {
                // Exposed wants the record complete before adding relationships
                this.locations = locations
                save()
            }

    /** For implementors of other record types having a reference. */
    fun from(record: SourceRecord) = Source(record, this)

    /** For implementors of other record types having a reference. */
    fun toRecord(source: Source) = source.record

    internal fun notifySaved(before: SourceResource?, after: SourceRecord?) =
            notifySaved(before, after?.let { from(it) }, publisher,
                    ::SourceResource, ::SourceSavedEvent)

    internal fun locationFrom(locationRecord: LocationRecord) =
            locations.from(locationRecord)

    internal fun toRecord(location: PersistedLocation) =
            locations.toRecord(location)
}

interface SourceDetails {
    val code: String
    val name: String
}

interface MutableSourceDetails {
    val code: String
    var name: String
}

data class SourceSavedEvent(
        val before: SourceResource?,
        val after: Source?) : ApplicationEvent(after ?: before)

class Source internal constructor(
        internal val record: SourceRecord,
        private val factory: Sources)
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
        other as Source
        return record == other.record
    }

    override fun hashCode() = record.hashCode()

    override fun toString() = "${super.toString()}{record=$record}"
}

class MutableSource internal constructor(
        private val snapshot: SourceResource?,
        private val record: SourceRecord,
        private val factory: Sources) : MutableSourceDetails by record {
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

object SourceRepository : CodeIdTable("SOURCE") {
    val code = text("code")
    val name = text("name")
}

class SourceRecord(id: EntityID<String>) : CodeEntity(id),
        SourceDetails,
        MutableSourceDetails {
    companion object : CodeEntityClass<SourceRecord>(SourceRepository)

    override var code by SourceRepository.code
    override var name by SourceRepository.name
    var locations by LocationRecord via SourceLocationsRepository

    override fun delete() {
        locations = emptySized()
        super.delete()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as SourceRecord
        return code == other.code
                && name == other.name
                && locations == other.locations
    }

    override fun hashCode() = Objects.hash(code, name, locations)

    override fun toString() =
            "${super.toString()}{id=$id, code=$code, name=$name, locations=$locations}"
}
