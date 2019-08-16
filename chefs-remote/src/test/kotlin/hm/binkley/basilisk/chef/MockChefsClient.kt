package hm.binkley.basilisk.chef

import io.micronaut.context.annotation.Replaces
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpResponse.created
import javax.inject.Singleton

@Replaces(ChefsOperations::class)
@Singleton
class MockChefsClient : ChefsOperations {
    override fun all() = emptyList<ChefResource>()

    override fun byCode(code: String) = null

    override fun new(chef: ChefResource) = created(chef)

    override fun update(code: String, chef: ChefResource) =
            HttpResponse.ok(chef)

    override fun delete(code: String, chef: ChefResource) =
            HttpResponse.ok(Unit)
}
