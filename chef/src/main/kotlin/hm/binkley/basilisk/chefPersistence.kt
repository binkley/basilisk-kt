package hm.binkley.basilisk

import io.micronaut.context.annotation.Context
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction
import javax.inject.Singleton

@Context
@Singleton
class ChefSchema(databaseSetup: DatabaseSetup) {
    private val seeSchemaInStdOut = true

    init {
        if (seeSchemaInStdOut) transaction {
            addLogger(StdOutSqlLogger)
            SchemaUtils.create(ChefRepository)
        }
    }
}
