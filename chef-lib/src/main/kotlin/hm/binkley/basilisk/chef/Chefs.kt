package hm.binkley.basilisk.chef

import hm.binkley.basilisk.chef.Chefs.Companion.FIT
import hm.binkley.basilisk.db.findOne
import hm.binkley.basilisk.domain.notifySaved
import io.micronaut.context.event.ApplicationEvent
import io.micronaut.context.event.ApplicationEventPublisher
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable
import java.util.*
import javax.inject.Singleton

interface Chefs {
    companion object {
        const val FIT = "FIT"
    }

    fun all(): Iterable<Chef>

    fun byCode(code: String): Chef?

    /** Saves a new chef in [FIT] health. */
    fun new(name: String, code: String, health: String = FIT): Chef;
}

@Singleton
class PersistedChefs(private val publisher: ApplicationEventPublisher)
    : Chefs {
    override fun all() = ChefRecord.all().map {
        from(it)
    }

    override fun byCode(code: String) = ChefRecord.findOne {
        ChefRepository.code eq code
    }?.let {
        from(it)
    }

    override fun new(name: String, code: String, health: String) =
            from(ChefRecord.new {
                this.name = name
                this.code = code
                this.health = health
            }).update(null) {
                save()
            }

    /** For implementors of other record types having a reference. */
    fun from(record: ChefRecord) = Chef(record, this)

    /** For implementors of other record types having a reference. */
    fun toRecord(chef: Chef) = chef.record

    internal fun notifySaved(before: ChefResource?, after: ChefRecord?) =
            notifySaved(before, after?.let { from(it) }, publisher,
                    ::ChefResource, ::ChefSavedEvent)
}

interface ChefDetails {
    val name: String
    val code: String
    val health: String
}

interface MutableChefDetails {
    var name: String
    var code: String
    var health: String
}

data class ChefSavedEvent(
        val before: ChefResource?,
        val after: Chef?) : ApplicationEvent(after ?: before)

class Chef internal constructor(
        internal val record: ChefRecord,
        private val factory: PersistedChefs)
    : ChefDetails by record {
    fun update(block: MutableChef.() -> Unit) =
            update(ChefResource(this), block)

    internal inline fun update(
            snapshot: ChefResource?,
            block: MutableChef.() -> Unit) = apply {
        MutableChef(snapshot, record, factory).block()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Chef
        return record == other.record
    }

    override fun hashCode() = record.hashCode()

    override fun toString() = "${super.toString()}{record=$record}"
}

class MutableChef internal constructor(
        private val snapshot: ChefResource?,
        private val record: ChefRecord,
        private val factory: PersistedChefs) : MutableChefDetails by record {
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
        other as MutableChef
        return snapshot == other.snapshot
                && record == other.record
    }

    override fun hashCode() = Objects.hash(snapshot, record)

    override fun toString() =
            "${super.toString()}{snapshot=$snapshot, record=$record}"
}

object ChefRepository : IntIdTable("CHEF") {
    val name = text("name")
    val code = text("code")
    val health = text("health")
}

class ChefRecord(id: EntityID<Int>) : IntEntity(id),
        ChefDetails,
        MutableChefDetails {
    companion object : IntEntityClass<ChefRecord>(ChefRepository)

    override var name by ChefRepository.name
    override var code by ChefRepository.code
    override var health by ChefRepository.health

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as ChefRecord
        return name == other.name
                && code == other.code
                && health == other.health
    }

    override fun hashCode() = Objects.hash(name, code, health)

    override fun toString() =
            "${super.toString()}{id=$id, name=$name, code=$code, health=$health}"
}
