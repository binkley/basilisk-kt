package hm.binkley.basilisk.chef

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.PathVariable
import org.jetbrains.exposed.sql.transactions.transaction
import java.net.URI

@Controller
class ChefsController(private val chefs: Chefs) : ChefsOperations {
    override fun all() = transaction {
        chefs.all().map {
            ChefResource(it)
        }
    }

    override fun byCode(@PathVariable code: String) =
            transaction {
                chefs.byCode(code)?.let {
                    ChefResource(it)
                }
            }

    override fun new(@Body chef: ChefResource) =
            transaction {
                chefs.new(chef.name, chef.code, chef.health).let {
                    HttpResponse.created(ChefResource(it),
                            URI.create("/chef/${it.code}"))
                }
            }
}
