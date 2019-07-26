package hm.binkley.basilisk

import io.micronaut.context.annotation.Context
import io.micronaut.context.annotation.Infrastructure
import org.jetbrains.exposed.sql.Database
import javax.sql.DataSource

@Context
@Infrastructure
class DatabaseSetup(dataSource: DataSource) {
    init {
        Database.connect(dataSource).apply {
            useNestedTransactions = true
        }
    }
}
