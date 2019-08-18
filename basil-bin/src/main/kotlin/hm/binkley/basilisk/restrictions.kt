package hm.binkley.basilisk

import hm.binkley.basilisk.ingredient.Ingredient
import hm.binkley.basilisk.location.PersistedLocation
import hm.binkley.basilisk.recipe.PersistedRecipe
import hm.binkley.basilisk.source.PersistedSource
import org.jetbrains.exposed.sql.EmptySizedIterable
import org.jetbrains.exposed.sql.SizedIterable

fun PersistedSource.restrictions() = this.locations

fun <I : Ingredient<I>> Ingredient<I>.restrictions(): SizedIterable<PersistedLocation> {
    val ingredientRestrictions = this.locations
    val sourceRestrictions = this.source.restrictions()

    if (!ingredientRestrictions.empty())
        return ingredientRestrictions

    return sourceRestrictions
}

fun PersistedRecipe.restrictions(): SizedIterable<PersistedLocation> {
    val recipeRestrictions = this.locations

    if (!recipeRestrictions.empty())
        return recipeRestrictions

    // TODO: Give the recipe ingredients
    return EmptySizedIterable()
}
