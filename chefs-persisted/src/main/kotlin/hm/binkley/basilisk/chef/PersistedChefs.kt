package hm.binkley.basilisk.chef

import hm.binkley.basilisk.db.CodeEntity
import hm.binkley.basilisk.db.CodeEntityClass
import hm.binkley.basilisk.db.CodeIdTable
import hm.binkley.basilisk.db.findOne
import hm.binkley.basilisk.domain.notifySaved
import io.micronaut.context.event.ApplicationEventPublisher
import org.jetbrains.exposed.dao.EntityID
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

    override fun new(chef: ChefResource) = ChefRecord.new {
        this.code = chef.code
        this.name = chef.name
        this.health = chef.health
    }.let {
        from(it)
    }.update(null) {
        save()
    }

    private fun from(record: ChefRecord) = PersistedChef(record, this)

    internal fun notifySaved(before: ChefResource?, after: ChefRecord?) =
            notifySaved(before, after?.let { from(it) }, publisher,
                    ::ChefResource, ::ChefSavedEvent)
}

class PersistedChef internal constructor(
        internal val record: ChefRecord,
        private val factory: PersistedChefs)
    : Chef,
        ChefDetails by record {
    private var snapshot: ChefResource? = ChefResource(this)

    /** @throws IllegalStateException if this chef has been deleted */
    override fun update(block: MutableChef.() -> Unit) =
            update(checkNotNull(snapshot), block)

    /** Used by [PersistedChefs.new] to indicate no initial snapshot */
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

object ChefRepository : CodeIdTable("CHEF") {
    val code = text("code")
    val name = text("name")
    val health = text("health")
}

class ChefRecord(id: EntityID<String>) : CodeEntity(id),
        ChefDetails,
        MutableChefDetails {
    companion object : CodeEntityClass<ChefRecord>(ChefRepository)

    override var code by ChefRepository.code
    override var name by ChefRepository.name
    override var health by ChefRepository.health

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as ChefRecord
        return code == other.code
                && name == other.name
                && health == other.health
    }

    override fun hashCode() = Objects.hash(code, name, health)

    override fun toString() =
            "${super.toString()}{id=$id, code=$code, name=$name, health=$health}"
}
