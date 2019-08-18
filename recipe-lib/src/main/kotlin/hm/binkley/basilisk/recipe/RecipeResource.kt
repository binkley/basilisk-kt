package hm.binkley.basilisk.recipe

import hm.binkley.basilisk.chef.ChefResource
import hm.binkley.basilisk.location.LocationResource

data class RecipeResource(
        val name: String,
        val code: String,
        val chef: ChefResource,
        val status: RecipeStatus,
        val locations: List<LocationResource>) {
    constructor(persistedRecipe: PersistedRecipe)
            : this(persistedRecipe.name, persistedRecipe.code,
            ChefResource(persistedRecipe.chef),
            persistedRecipe.status,
            persistedRecipe.locations.map { LocationResource(it) })
}
