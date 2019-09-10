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
class DataPersistedChefs(
        private val repository: DataChefRepository,
        private val publisher: ApplicationEventPublisher)
    : Chefs {
    override fun all() = repository.findAll().map {
        DataPersistedChef(ChefResource(it), it, this)
    }

    override fun byCode(code: String) =
            repository.findById(code).orElse(null)?.let {
                DataPersistedChef(ChefResource(it), it, this)
            }

    override fun new(chef: ChefResource) =
            DataPersistedChef(null, DataChefRecord(chef), this)

    internal fun save(record: DataChefRecord) =
            repository.upsert(record.code, record.name, record.health)

    internal fun delete(record: DataChefRecord) =
            repository.delete(record)

    internal fun notifyChanged(event: ChefChangedEvent) =
            notifyChanged(event.before, event.after,
                    publisher, ::ChefChangedEvent)
}

class DataPersistedChef(
        private var snapshot: ChefResource?,
        private val record: DataChefRecord,
        private val factory: DataPersistedChefs)
    : Chef,
        ChefDetails by record {
    override fun update(block: MutableChef.() -> Unit) = apply {
        DataPersistedMutableChef(::snapshot, record, factory).block()
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
        private val snapshot: KMutableProperty0<ChefResource?>,
        private val record: DataChefRecord,
        private val factory: DataPersistedChefs)
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

    // TODO: Micronaut Data ignores default, overridden "save"
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
