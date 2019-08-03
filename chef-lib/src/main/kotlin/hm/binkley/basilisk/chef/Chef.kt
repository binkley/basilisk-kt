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

    fun chef(record: ChefRecord) =
            Chef(record, this)

    fun create(name: String, code: String) = Chef(
            ChefRecord.new {
                this.name = name
                this.code = code
            }, this).save()

    fun all() = ChefRecord.all().map {
        chef(it)
    }

    internal fun notifySaved(chef: Chef) {
        publisher.publishEvent(ChefSavedEvent(chef))
    }

    override fun toString() =
            "${super.toString()}{publisher=$publisher}"
}

interface ChefRecordData {
    val name: String
    val code: String
}

data class ChefSavedEvent(val after: Chef) : ApplicationEvent(after)

class Chef(
        private val record: ChefRecord,
        private val factory: Chefs)
    : ChefRecordData by record {
    fun save(): Chef {
        record.flush()
        factory.notifySaved(this)
        return this
    }

    override fun toString() = "${super.toString()}{record=$record}"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Chef

        return record == other.record
    }

    override fun hashCode() = record.hashCode()
}

object ChefRepository : IntIdTable("CHEF") {
    val name = text("name")
    val code = text("code")
}

class ChefRecord(id: EntityID<Int>) : IntEntity(id),
        ChefRecordData {
    companion object : IntEntityClass<ChefRecord>(ChefRepository)

    override var name by ChefRepository.name
    override var code by ChefRepository.code

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
