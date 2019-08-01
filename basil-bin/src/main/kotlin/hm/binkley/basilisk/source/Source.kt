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
    fun source(code: String) = SourceRecord.findOne {
        SourceRepository.code eq code
    }?.let {
        source(it)
    }

    fun source(record: SourceRecord) = Source(record, this)

    fun create(name: String, code: String) = Source(SourceRecord.new {
        this.name = name
        this.code = code
    }, this).save()

    internal fun notifySaved(source: Source) {
        publisher.publishEvent(SourceSavedEvent(source))
    }

    internal fun locationFor(locationRecord: LocationRecord) =
            locations.location(locationRecord)
}

interface SourceDetails {
    val name: String
    val code: String
}

data class SourceSavedEvent(val source: Source) : ApplicationEvent(source)

class Source(
        private val record: SourceRecord,
        private val factory: Sources)
    : SourceDetails by record {
    val locations: SizedIterable<Location>
        get() = record.locations.mapLazy {
            factory.locationFor(it)
        }

    fun save(): Source {
        record.flush()
        factory.notifySaved(this)
        return this
    }

    override fun toString(): String {
        return "${super.toString()}{record=$record}"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Source

        return record == other.record
    }

    override fun hashCode(): Int {
        return record.hashCode()
    }
}

object SourceRepository : IntIdTable("SOURCE") {
    val name = text("name")
    val code = text("code")
}

class SourceRecord(id: EntityID<Int>) : IntEntity(id),
        SourceDetails {
    companion object : IntEntityClass<SourceRecord>(SourceRepository)

    override var name by SourceRepository.name
    override var code by SourceRepository.code
    var locations by LocationRecord via SourceLocationsRepository

    override fun toString(): String {
        return "${super.toString()}{id=$id, name=$name, code=$code}"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as SourceRecord
        return name == other.name
                && code == other.code
                && locations == other.locations
    }

    override fun hashCode(): Int {
        return Objects.hash(name, code, locations)
    }
}
