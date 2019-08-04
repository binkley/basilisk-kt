package hm.binkley.basilisk.source

import hm.binkley.basilisk.db.findOne
import hm.binkley.basilisk.location.Location
import hm.binkley.basilisk.location.LocationRecord
import hm.binkley.basilisk.location.Locations
import io.micronaut.context.event.ApplicationEvent
import io.micronaut.context.event.ApplicationEventPublisher
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.sql.SizedIterable
import org.jetbrains.exposed.sql.mapLazy
import java.util.*
import javax.inject.Singleton

@Singleton
class Sources(
        private val locations: Locations,
        private val publisher: ApplicationEventPublisher) {
    fun byCode(code: String) = SourceRecord.findOne {
        SourceRepository.code eq code
    }?.let {
        from(it)
    }

    fun new(name: String, code: String) = from(SourceRecord.new {
        this.name = name
        this.code = code
    }).update(null) {
        save()
    }

    internal fun from(record: SourceRecord) = Source(record, this)

    internal fun notifySaved(
            before: SourceResource?,
            after: SourceRecord?) {
        publisher.publishEvent(SourceSavedEvent(
                before, after?.let { from(it) }))
    }

    internal fun locationFor(locationRecord: LocationRecord) =
            locations.from(locationRecord)
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
        val after: Source?) : ApplicationEvent(after ?: before)

class Source internal constructor(
        private val record: SourceRecord,
        private val factory: Sources)
    : SourceDetails by record {
    val locations: SizedIterable<Location>
        get() = record.locations.notForUpdate().mapLazy {
            factory.locationFor(it)
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
    fun save() = apply {
        record.flush() // TODO: Aggressively flush, or wait for txn to end?
        factory.notifySaved(snapshot, record)
    }

    fun delete() {
        record.delete() // TODO: Detect if edited, and not saved, then deleted
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

    override fun toString() =
            "${super.toString()}{id=$id, name=$name, code=$code}"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as SourceRecord
        return name == other.name
                && code == other.code
                && locations == other.locations
    }

    override fun hashCode() = Objects.hash(name, code, locations)
}
