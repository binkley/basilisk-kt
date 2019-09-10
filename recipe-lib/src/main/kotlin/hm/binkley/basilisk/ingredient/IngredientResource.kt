package hm.binkley.basilisk.ingredient

import hm.binkley.basilisk.chef.ChefResource
import hm.binkley.basilisk.location.LocationResource
import hm.binkley.basilisk.recipe.RecipeResource
import hm.binkley.basilisk.source.SourceResource

data class IngredientResource(
        override val source: SourceResource,
        override val code: String,
        override val name: String,
        override val chef: ChefResource,
        override val recipe: RecipeResource?,
        override val locations: List<LocationResource>) : IngredientDetails {
    constructor(ingredient: IngredientDetails)
            : this(SourceResource(ingredient.source),
            ingredient.code,
            ingredient.name,
            ChefResource(ingredient.chef),
            ingredient.recipe?.let { RecipeResource(it) },
            ingredient.locations.map { LocationResource(it) })
}
