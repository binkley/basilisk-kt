package hm.binkley.basilisk.recipe

import hm.binkley.basilisk.chef.ChefResource
import hm.binkley.basilisk.location.LocationResource

data class RecipeResource(
        val code: String,
        val name: String,
        val chef: ChefResource,
        val status: RecipeStatus,
        val locations: List<LocationResource>) {
    constructor(recipe: RecipeDetails)
            : this(recipe.code,
            recipe.name,
            ChefResource(recipe.chef),
            recipe.status,
            recipe.locations.map { LocationResource(it) })
}
