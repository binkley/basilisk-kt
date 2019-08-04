package hm.binkley.basilisk.ingredient

import hm.binkley.basilisk.chef.ChefResource
import hm.binkley.basilisk.location.LocationResource

data class IngredientResource(
        val name: String,
        val code: String,
        val chef: ChefResource,
        val locations: List<LocationResource>) {
    constructor(ingredient: Ingredient)
            : this(ingredient.name, ingredient.code,
            ChefResource(ingredient.chef),
            ingredient.locations.map { LocationResource(it) })
}
