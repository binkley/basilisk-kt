package x.micronaut

import io.micronaut.context.annotation.Context
import io.micronaut.context.annotation.Infrastructure
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction
import javax.sql.DataSource

@Context
@Infrastructure
class DatabaseSetup(dataSource: DataSource) {
    private val seeSchemaInStdOut = true

    init {
        Database.connect(dataSource)
        if (seeSchemaInStdOut) transaction {
            addLogger(StdOutSqlLogger)
            SchemaUtils.create(LocationRepository, IngredientRepository, RecipeRepository, ChefRepository)
        }
    }
}
