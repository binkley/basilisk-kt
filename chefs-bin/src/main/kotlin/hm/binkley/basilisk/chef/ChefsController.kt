package hm.binkley.basilisk.chef

import io.micronaut.http.annotation.Controller
import org.jetbrains.exposed.sql.transactions.transaction

@Controller("/chefs")
class ChefsController(private val chefs: Chefs) : ChefsOperations {
    override fun all() = transaction {
        chefs.all().map {
            ChefResource(it)
        }
    }
}
