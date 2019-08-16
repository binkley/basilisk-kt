package hm.binkley.basilisk.chef

import hm.binkley.basilisk.chef.Chefs.Companion.FIT
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.Post
import io.micronaut.validation.Validated

@Validated
interface ChefsOperations {
    @Get("/chefs")
    fun all(): Iterable<ChefResource>

    @Get("/chef/{code}")
    fun byCode(@PathVariable code: String): ChefResource?

    /** Saves a new chef in [FIT] health. */
    @Post("/chefs")
    fun new(@Body chef: ChefResource): HttpResponse<ChefResource>
}
