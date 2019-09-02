package hm.binkley.basilisk.chef

import hm.binkley.basilisk.domain.notifySaved
import io.micronaut.context.event.ApplicationEventPublisher
import io.micronaut.data.annotation.Query
import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.query.builder.sql.Dialect.POSTGRES
import io.micronaut.data.repository.CrudRepository
import java.util.*
import javax.inject.Singleton
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Singleton
class DataPersistedChefs(
        private val repository: DataChefRepository,
        private val publisher: ApplicationEventPublisher)
    : Chefs {
    override fun all() = repository.findAll().map {
        DataPersistedChef(it, this)
    }

    override fun byCode(code: String) =
            repository.findById(code).orElse(null)?.let {
                DataPersistedChef(it, this)
            }

    override fun new(chef: ChefResource) =
            DataPersistedChef(DataChefRecord(chef), this).update(null) {
                save()
            }

    internal fun save(record: DataChefRecord) =
            DataPersistedChef(repository.upsert(
                    record.code, record.name, record.health),
                    this)

    internal fun delete(record: DataChefRecord) =
            repository.delete(record)

    internal fun notifySaved(before: ChefResource?, after: DataChefRecord?) =
            notifySaved(before, after?.let { DataPersistedChef(it, this) },
                    publisher,
                    ::ChefResource, ::ChefSavedEvent)
}

class DataPersistedChef(
        internal val record: DataChefRecord,
        private val factory: DataPersistedChefs)
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
        DataPersistedMutableChef(snapshot, { newSnapshot ->
            this.snapshot = newSnapshot
        }, record, factory).block()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as DataPersistedChef
        return snapshot == other.snapshot
                && record == other.record
    }

    override fun hashCode() = Objects.hash(snapshot, record)

    override fun toString() =
            "${super.toString()}{snapshot=$snapshot, record=$record}"
}

class DataPersistedMutableChef internal constructor(
        private val snapshot: ChefResource?,
        private val setSnapshot: (ChefResource?) -> Unit,
        private val record: DataChefRecord,
        private val factory: DataPersistedChefs)
    : MutableChef,
        MutableChefDetails by record {
    override fun save() = apply {
        factory.save(record)
        factory.notifySaved(snapshot, record)
        setSnapshot(ChefResource(record))
    }

    override fun delete() {
        factory.delete(record)
        factory.notifySaved(snapshot, null)
        setSnapshot(null)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as DataPersistedMutableChef
        return snapshot == other.snapshot
                && record == other.record
    }

    override fun hashCode() = Objects.hash(snapshot, record)

    override fun toString() =
            "${super.toString()}{snapshot=$snapshot, record=$record}"
}

@JdbcRepository(dialect = POSTGRES)
interface DataChefRepository : CrudRepository<DataChefRecord, String> {
    @Query("""
        INSERT INTO chef (code, name, health)
        VALUES (:code, :name, :health)
        ON CONFLICT (code) DO UPDATE
        SET name = :name, health = :health
        RETURNING *""")
    fun upsert(code: String, name: String, health: String): DataChefRecord
}

@Entity
@Table(name = "chef")
data class DataChefRecord(
        @Id override val code: String,
        override var name: String,
        override var health: String)
    : ChefDetails,
        MutableChefDetails {
    constructor(chef: ChefResource) : this(chef.code, chef.name, chef.health)
}
