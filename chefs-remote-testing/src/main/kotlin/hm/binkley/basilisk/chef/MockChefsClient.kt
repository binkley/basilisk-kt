package hm.binkley.basilisk.chef

import io.micronaut.context.annotation.Replaces
import io.micronaut.http.HttpResponse.created
import io.micronaut.http.HttpResponse.ok
import javax.inject.Singleton

@Replaces(ChefsClient::class)
@Singleton
class MockChefsClient : ChefsClient {
    var all = emptyList<ChefResource>()
    var one: ChefResource? = null

    fun reset() {
        all = emptyList()
        one = null
    }

    override fun all() = all

    override fun byCode(code: String): ChefResource? {
        val one = this.one
        return when {
            null == one -> null
            code == one.code -> one
            else -> throw AssertionError(
                    "Mock mismatch: expected match for code: $code; mocked is $one")
        }
    }

    override fun new(chef: ChefResource) = created(chef)!!.also {
        one = chef
    }

    override fun update(code: String, chef: ChefResource) = chef.also {
        one = chef
    }

    override fun delete(code: String) = ok(Unit)!!
}
