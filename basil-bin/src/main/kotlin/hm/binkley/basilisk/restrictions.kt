package hm.binkley.basilisk

import hm.binkley.basilisk.ingredient.Ingredient
import hm.binkley.basilisk.location.Location
import hm.binkley.basilisk.recipe.Recipe
import hm.binkley.basilisk.source.Source
import org.jetbrains.exposed.sql.EmptySizedIterable
import org.jetbrains.exposed.sql.SizedIterable

fun Source.restrictions() = this.locations

fun <I : Ingredient<I>> Ingredient<I>.restrictions(): SizedIterable<Location> {
    val ingredientRestrictions = this.locations
    val sourceRestrictions = this.source.restrictions()

    if (!ingredientRestrictions.empty())
        return ingredientRestrictions

    return sourceRestrictions
}

fun Recipe.restrictions(): SizedIterable<Location> {
    val recipeRestrictions = this.locations

    if (!recipeRestrictions.empty())
        return recipeRestrictions
    // TODO: Give the recipe ingredients
    return EmptySizedIterable()
}
