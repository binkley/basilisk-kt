package hm.binkley.basilisk

import hm.binkley.basilisk.ingredient.Ingredient
import hm.binkley.basilisk.location.Location
import hm.binkley.basilisk.recipe.Recipe
import hm.binkley.basilisk.source.Source
import org.jetbrains.exposed.sql.SizedCollection
import org.jetbrains.exposed.sql.SizedIterable

fun Source.restrictions() = this.locations

fun <I : Ingredient<I>> Ingredient<I>.restrictions(): SizedIterable<Location> {
    val ingredientRestrictions = this.locations
    if (!ingredientRestrictions.empty())
        return ingredientRestrictions

    return source.restrictions()
}

fun Recipe.restrictions(): SizedIterable<Location> {
    val recipeRestrictions = this.locations
    if (!recipeRestrictions.empty())
        return recipeRestrictions

    return this.ingredients.map {
        it.restrictions()
    }.filter {
        !it.empty()
    }.reduce { a, b ->
        a.intersect(b)
    }
}

fun <T> SizedIterable<T>.intersect(that: SizedIterable<T>) =
        SizedCollection(this.toList().intersect(that.toList()))
