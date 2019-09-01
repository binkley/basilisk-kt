package hm.binkley.basilisk.db

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IdTable
import org.jetbrains.exposed.sql.Column

abstract class CodeIdTable(name: String = "", columnName: String = "code") :
        IdTable<String>(name) {
    override val id: Column<EntityID<String>> =
            text(columnName).primaryKey().entityId()
}

abstract class CodeEntity(id: EntityID<String>) : Entity<String>(id)

abstract class CodeEntityClass<out E : CodeEntity>(table: IdTable<String>,
        entityType: Class<E>? = null) :
        EntityClass<String, E>(table, entityType)
