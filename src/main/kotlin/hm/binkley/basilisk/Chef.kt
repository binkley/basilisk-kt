package hm.binkley.basilisk

import io.micronaut.context.event.ApplicationEvent
import io.micronaut.context.event.ApplicationEventPublisher
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable
import javax.inject.Singleton

@Singleton
class Chefs(private val publisher: ApplicationEventPublisher) {
    fun chef(code: String): Chef? {
        val record = ChefRecord.findOne {
            ChefRepository.code eq code
        }

        return record?.let { chef(it) }
    }

    fun chef(record: ChefRecord) = Chef(record, publisher)

    fun create(name: String, code: String) = Chef(ChefRecord.new {
        this.name = name
        this.code = code
    }, publisher).save()
}

interface ChefRecordData {
    val name: String
    val code: String
}

data class ChefSavedEvent(val chef: Chef)
    : ApplicationEvent(chef)

class Chef(
        private val record: ChefRecord,
        private val publisher: ApplicationEventPublisher)
    : ChefRecordData by record {
    fun save(): Chef {
        record.flush()
        publisher.publishEvent(ChefSavedEvent(this))
        return this
    }
}

object ChefRepository : IntIdTable("CHEF") {
    val name = text("name")
    val code = text("code")
}

class ChefRecord(id: EntityID<Int>) : IntEntity(id), ChefRecordData {
    companion object : IntEntityClass<ChefRecord>(ChefRepository)

    override var name by ChefRepository.name
    override var code by ChefRepository.code
}
