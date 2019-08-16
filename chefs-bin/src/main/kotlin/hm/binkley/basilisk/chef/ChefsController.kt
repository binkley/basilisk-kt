package hm.binkley.basilisk.chef

import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpResponse.created
import io.micronaut.http.HttpResponse.ok
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.PathVariable
import org.jetbrains.exposed.sql.transactions.transaction
import java.net.URI

@Controller
class ChefsController(private val chefs: PersistedChefs) : ChefsOperations {
    override fun all() = transaction {
        chefs.all()
    }.map {
        ChefResource(it)
    }

    override fun byCode(@PathVariable code: String) =
            transaction {
                chefs.byCode(code)
            }?.let {
                ChefResource(it)
            }

    override fun new(@Body chef: ChefResource) =
            transaction {
                chefs.new(chef)
            }.let {
                ChefResource(it)
            }.let {
                created(it, URI.create("/chef/${it.code}"))
            }

    override fun update(@PathVariable code: String,
                        @Body chef: ChefResource): HttpResponse<ChefResource> =
            transaction {
                chefs.byCode(code)!!.update {
                    this.name = chef.name
                    this.health = chef.health
                    save()
                }
            }.let {
                ChefResource(it)
            }.let {
                ok(it)
            }

    override fun delete(@PathVariable code: String,
                        @Body chef: ChefResource): HttpResponse<Unit> =
            transaction {
                chefs.byCode(code)!!.update {
                    delete()
                }
            }.let {
                ok()
            }
}
