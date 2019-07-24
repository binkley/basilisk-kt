package hm.binkley.basilisk

import io.micronaut.context.event.ApplicationEvent
import io.micronaut.context.event.ApplicationEventPublisher
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable
import java.util.*
import javax.inject.Singleton

@Singleton
class Sources(private val publisher: ApplicationEventPublisher) {
    fun source(code: String): Source? {
        val record = SourceRecord.findOne {
            SourceRepository.code eq code
        }

        return record?.let { source(it) }
    }

    fun source(record: SourceRecord) = Source(record, publisher)

    fun create(name: String, code: String) = Source(SourceRecord.new {
        this.name = name
        this.code = code
    }, publisher).save()
}

interface SourceRecordData {
    val name: String
    val code: String
}

data class SourceSavedEvent(val source: Source)
    : ApplicationEvent(source)

class Source(
        private val record: SourceRecord,
        private val publisher: ApplicationEventPublisher)
    : SourceRecordData by record {
    fun save(): Source {
        record.flush()
        publisher.publishEvent(SourceSavedEvent(this))
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

class SourceRecord(id: EntityID<Int>) : IntEntity(id), SourceRecordData {
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
