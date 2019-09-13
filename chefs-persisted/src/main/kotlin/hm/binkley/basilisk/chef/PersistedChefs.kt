package hm.binkley.basilisk.chef

import hm.binkley.basilisk.domain.notifyChanged
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
import kotlin.reflect.KMutableProperty0

@Singleton
class PersistedChefs(
        private val repository: ChefRepository,
        private val publisher: ApplicationEventPublisher)
    : Chefs {
    override fun all() = repository.findAll().map {
        PersistedChef(ChefResource(it), it, this)
    }

    override fun byCode(code: String) =
            repository.findById(code).orElse(null)?.let {
                PersistedChef(ChefResource(it), it, this)
            }

    override fun new(chef: ChefResource) =
            PersistedChef(null, ChefRecord(chef), this)

    internal fun save(record: ChefRecord) =
            repository.upsert(record.code, record.name, record.health)

    internal fun delete(record: ChefRecord) =
            repository.delete(record)

    internal fun notifyChanged(event: ChefChangedEvent) =
            notifyChanged(event.before, event.after,
                    publisher, ::ChefChangedEvent)
}

class PersistedChef(
        private var snapshot: ChefResource?,
        private val record: ChefRecord,
        private val factory: PersistedChefs)
    : Chef,
        ChefDetails by record {
    override fun update(block: MutableChef.() -> Unit) = apply {
        PersistedMutableChef(::snapshot, record, factory).block()
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
        private val snapshot: KMutableProperty0<ChefResource?>,
        private val record: ChefRecord,
        private val factory: PersistedChefs)
    : MutableChef,
        MutableChefDetails by record {
    override fun save() = apply {
        factory.save(record)
        factory.notifyChanged(
                ChefChangedEvent(snapshot.get(), ChefResource(record)))
        snapshot.set(ChefResource(record))
    }

    override fun delete() {
        factory.delete(record)
        factory.notifyChanged(
                ChefChangedEvent(snapshot.get(), null))
        snapshot.set(null)
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

@JdbcRepository(dialect = POSTGRES)
interface ChefRepository : CrudRepository<ChefRecord, String> {
    @Query("""
        INSERT INTO chef (code, name, health)
        VALUES (:code, :name, :health)
        ON CONFLICT (code) DO UPDATE
        SET name = :name, health = :health
        RETURNING *""")
    fun upsert(code: String, name: String, health: String): ChefRecord

    // TODO: Micronaut Data ignores default, overridden "save"
}

@Entity
@Table(name = "chef")
data class ChefRecord(
        @Id override val code: String,
        override var name: String,
        override var health: String)
    : ChefDetails,
        MutableChefDetails {
    constructor(chef: ChefResource) : this(chef.code, chef.name, chef.health)
}
