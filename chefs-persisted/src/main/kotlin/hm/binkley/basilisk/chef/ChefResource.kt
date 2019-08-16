package hm.binkley.basilisk.chef

import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.NotNull

@Introspected
data class ChefResource(
        @NotNull val name: String,
        @NotNull val code: String,
        @NotNull val health: String) {
    constructor(chef: Chef) : this(chef.name, chef.code, chef.health)
}
