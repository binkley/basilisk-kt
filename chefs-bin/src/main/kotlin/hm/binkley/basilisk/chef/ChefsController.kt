package hm.binkley.basilisk.chef

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import org.jetbrains.exposed.sql.transactions.transaction

@Controller("/chefs")
class ChefsController(private val chefs: Chefs) {
    @Get
    fun all() = transaction {
        chefs.all().map {
            ChefResource(it)
        }
    }
}
