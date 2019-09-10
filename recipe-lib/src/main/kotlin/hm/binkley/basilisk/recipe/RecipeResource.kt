package hm.binkley.basilisk.recipe

import hm.binkley.basilisk.chef.ChefResource
import hm.binkley.basilisk.location.LocationResource

data class RecipeResource(
        override val code: String,
        override val name: String,
        override val chef: ChefResource,
        override val status: RecipeStatus,
        override val locations: List<LocationResource>) : RecipeDetails {
    constructor(recipe: RecipeDetails)
            : this(recipe.code,
            recipe.name,
            ChefResource(recipe.chef),
            recipe.status,
            recipe.locations.map { LocationResource(it) })
}
