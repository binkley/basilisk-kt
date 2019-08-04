package hm.binkley.basilisk.recipe

import hm.binkley.basilisk.chef.ChefResource
import hm.binkley.basilisk.location.LocationResource

data class RecipeResource(
        val name: String,
        val code: String,
        val chef: ChefResource,
        val status: RecipeStatus,
        val locations: List<LocationResource>) {
    constructor(recipe: Recipe)
            : this(recipe.name, recipe.code, ChefResource(recipe.chef),
            recipe.status, recipe.locations.map { LocationResource(it) })
}
