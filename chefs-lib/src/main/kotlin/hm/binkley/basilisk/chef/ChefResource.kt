package hm.binkley.basilisk.chef

import hm.binkley.basilisk.chef.Chefs.Companion.FIT
import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.NotNull

@Introspected
data class ChefResource(
        @NotNull override val name: String,
        @NotNull override val code: String,
        @NotNull override val health: String = FIT) : ChefDetails {
    constructor(chef: ChefDetails) : this(chef.name, chef.code, chef.health)
}
