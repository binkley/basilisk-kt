package hm.binkley.basilisk.chef

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

    override fun new(chef: ChefResource) = from(ChefRecord.new {
        this.name = chef.name
        this.code = chef.code
        this.health = chef.health
    }).update(null) {
        save()
    }

    /** For implementors of other record types having a reference. */
    fun from(record: ChefRecord) = PersistedChef(record, this)

    internal fun notifySaved(before: ChefResource?, after: ChefRecord?) =
            notifySaved(before, after?.let { from(it) }, publisher,
                    ::ChefResource, ::ChefSavedEvent)
}

data class ChefSavedEvent(
        val before: ChefResource?,
        val after: Chef?) : ApplicationEvent(after ?: before)

class PersistedChef internal constructor(
        internal val record: ChefRecord,
        private val factory: PersistedChefs)
    : Chef,
        ChefDetails by record {
    private var snapshot: ChefResource? = ChefResource(this)

    /**
     * @throws IllegalStateException if this chef has been deleted
     */
    override fun update(block: MutableChef.() -> Unit) =
            update(checkNotNull(snapshot), block)

    internal inline fun update(
            snapshot: ChefResource?,
            block: MutableChef.() -> Unit) = apply {
        PersistedMutableChef(snapshot, { newSnapshot ->
            this.snapshot = newSnapshot
        }, record, factory).block()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as PersistedChef
        return snapshot == other.snapshot
                && record == other.record
    }

    override fun hashCode() = Objects.hash(snapshot, record)

    override fun toString() =
            "${super.toString()}{snapshot=$snapshot, record=$record}"
}

class PersistedMutableChef internal constructor(
        private val snapshot: ChefResource?,
        private val setSnapshot: (ChefResource?) -> Unit,
        private val record: ChefRecord,
        private val factory: PersistedChefs)
    : MutableChef,
        MutableChefDetails by record {
    override fun save() = apply {
        record.flush()
        factory.notifySaved(snapshot, record)
        setSnapshot(ChefResource(record))
    }

    override fun delete() {
        record.delete()
        factory.notifySaved(snapshot, null)
        setSnapshot(null)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as PersistedMutableChef
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
