package hm.binkley.basilisk.chef

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
class Chefs(private val publisher: ApplicationEventPublisher) {
    fun chef(code: String): Chef? {
        val record = ChefRecord.findOne {
            ChefRepository.code eq code
        }

        return record?.let { chef(it) }
    }

    fun chef(record: ChefRecord) = Chef(record, this)

    fun new(name: String, code: String): Chef {
        val record = ChefRecord.new {
            this.name = name
            this.code = code
        }
        MutableChef(null, record, this).save()
        return Chef(record, this)
    }

    fun all() = ChefRecord.all().map {
        chef(it)
    }

    internal fun notifySaved(before: ChefResource?, after: MutableChef?) {
        publisher.publishEvent(ChefSavedEvent(before, after))
    }

    override fun toString() =
            "${super.toString()}{publisher=$publisher}"
}

interface ChefRecordData {
    val name: String
    val code: String
}

interface MutableChefRecordData {
    var name: String
    var code: String
}

data class ChefSavedEvent(
        val before: ChefResource?,
        val after: MutableChef?) : ApplicationEvent(after ?: before)

class Chef(
        private val record: ChefRecord,
        private val factory: Chefs)
    : ChefRecordData by record {
    fun mutable() = MutableChef(record.snapshot(), record, factory)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Chef

        return record == other.record
    }

    override fun hashCode() = record.hashCode()

    override fun toString() = "${super.toString()}{record=$record}"
}

class MutableChef(
        private val snapshot: ChefResource?,
        private val record: ChefRecord,
        private val factory: Chefs) : MutableChefRecordData by record {
    fun save(): MutableChef {
        record.flush() // TODO: Aggressively flush, or wait for txn to end?
        factory.notifySaved(snapshot, this)
        return this
    }

    fun delete() {
        // TODO: Detect if edited, and not saved, then deleted
        record.delete()
        factory.notifySaved(snapshot, null)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MutableChef

        return record == other.record
    }

    override fun hashCode() = record.hashCode()

    override fun toString() = "${super.toString()}{record=$record}"
}

object ChefRepository : IntIdTable("CHEF") {
    val name = text("name")
    val code = text("code")
}

class ChefRecord(id: EntityID<Int>) : IntEntity(id),
        ChefRecordData,
        MutableChefRecordData {
    companion object : IntEntityClass<ChefRecord>(ChefRepository)

    override var name by ChefRepository.name
    override var code by ChefRepository.code

    fun snapshot() = ChefResource(name, code)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as ChefRecord
        return name == other.name
                && code == other.code
    }

    override fun hashCode() = Objects.hash(name, code)

    override fun toString() =
            "${super.toString()}{id=$id, name=$name, code=$code}"
}
