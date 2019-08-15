package hm.binkley.basilisk.chef

import io.micronaut.http.annotation.Get
import io.micronaut.validation.Validated

@Validated
interface ChefsOperations {
    @Get
    fun all(): List<ChefResource>
}
