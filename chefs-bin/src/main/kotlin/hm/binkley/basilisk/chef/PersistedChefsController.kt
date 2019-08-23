package hm.binkley.basilisk.chef

import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpResponse.created
import io.micronaut.http.HttpResponse.ok
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Error
import io.micronaut.http.annotation.PathVariable
import org.jetbrains.exposed.sql.transactions.transaction
import java.net.URI

@Controller
class PersistedChefsController(private val chefs: PersistedChefs)
    : ChefsOperations {
    override fun all() = transaction {
        chefs.all()
    }.map {
        ChefResource(it)
    }

    override fun byCode(@PathVariable code: String) = transaction {
        byCodeOrThrow(code)
    }.let {
        ChefResource(it)
    }

    override fun new(@Body chef: ChefResource) = transaction {
        chefs.new(chef)
    }.let {
        ChefResource(it)
    }.let {
        created<ChefResource>(it, URI.create("/chef/${it.code}"))!!
    }

    override fun update(@PathVariable code: String,
            @Body chef: ChefResource) = transaction {
        byCodeOrThrow(code).update {
            this.name = chef.name
            this.health = chef.health
            save()
        }
    }.let {
        ChefResource(it)
    }

    override fun delete(@PathVariable code: String): HttpResponse<Unit> =
            transaction {
                byCodeOrThrow(code).update {
                    delete()
                }
            }.let {
                ok<Unit>()
            }

    private fun byCodeOrThrow(code: String): PersistedChef {
        return chefs.byCode(code) ?: throw NotFound("No chef for $code")
    }

    class NotFound(message: String) : Exception(message)

    @Error(NotFound::class)
    fun notFound(request: HttpRequest<*>, e: Throwable) =
            HttpResponse.notFound<String>(e.localizedMessage)!!
}
