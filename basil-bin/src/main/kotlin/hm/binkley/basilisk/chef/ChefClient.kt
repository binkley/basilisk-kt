package hm.binkley.basilisk.chef

import io.micronaut.http.client.annotation.Client
import io.micronaut.retry.annotation.Retryable

@Client("http://localhost:7372/chefs")
@Retryable
interface ChefClient : ChefsOperations {
    override fun all(): List<ChefResource>
}
