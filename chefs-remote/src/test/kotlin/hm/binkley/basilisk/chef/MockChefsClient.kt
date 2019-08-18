package hm.binkley.basilisk.chef

import io.micronaut.context.annotation.Replaces
import io.micronaut.http.HttpResponse.created
import io.micronaut.http.HttpResponse.ok
import javax.inject.Singleton

@Replaces(ChefsOperations::class)
@Singleton
class MockChefsClient : ChefsOperations {
    var all = emptyList<ChefResource>()
    var one: ChefResource? = null

    fun reset() {
        all = emptyList()
        one = null
    }

    override fun all() = all

    override fun byCode(code: String) = one

    override fun new(chef: ChefResource) = created(chef)

    override fun update(code: String, chef: ChefResource) = ok(chef)

    override fun delete(code: String, chef: ChefResource) = ok(Unit)
}
