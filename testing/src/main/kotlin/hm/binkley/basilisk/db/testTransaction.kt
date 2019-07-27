package hm.binkley.basilisk.db

import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction

fun <T> testTransaction(statement: Transaction.() -> T): T = transaction {
    val result = statement()
    rollback()
    result
}
