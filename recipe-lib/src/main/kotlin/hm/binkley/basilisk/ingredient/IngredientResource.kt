package hm.binkley.basilisk.ingredient

import hm.binkley.basilisk.chef.ChefResource
import hm.binkley.basilisk.location.LocationResource
import hm.binkley.basilisk.source.SourceResource

data class IngredientResource(
        val source: SourceResource,
        val code: String,
        val chef: ChefResource,
        val locations: List<LocationResource>) {
    constructor(ingredient: Ingredient)
            : this(SourceResource(ingredient.source),
            ingredient.code,
            ChefResource(ingredient.chef),
            ingredient.locations.map { LocationResource(it) })
}
