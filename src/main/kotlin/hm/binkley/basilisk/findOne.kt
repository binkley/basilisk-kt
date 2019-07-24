package hm.binkley.basilisk

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder

fun <ID : Comparable<ID>, T : Entity<ID>> EntityClass<ID, T>.findOne(
        op: SqlExpressionBuilder.() -> Op<Boolean>): T? {
    val found = find(op)
    return when (found.count()) {
        0 -> null
        1 -> found.first()
        else -> {
            throw IllegalStateException() // TODO: Better exception
        }
    }
}
