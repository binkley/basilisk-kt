package hm.binkley.basilisk

import hm.binkley.basilisk.ingredient.Ingredient
import hm.binkley.basilisk.location.Location
import hm.binkley.basilisk.recipe.Recipe
import hm.binkley.basilisk.source.Source
import org.jetbrains.exposed.sql.SizedCollection
import org.jetbrains.exposed.sql.SizedIterable
import org.jetbrains.exposed.sql.emptySized

fun Source.restrictions() = locations

fun <I : Ingredient<I>> Ingredient<I>.restrictions(): SizedIterable<Location> {
    val ingredientRestrictions = locations
    if (!ingredientRestrictions.empty())
        return ingredientRestrictions

    return source.restrictions()
}

fun Recipe.restrictions(): SizedIterable<Location> {
    val recipeRestrictions = locations
    if (!recipeRestrictions.empty())
        return recipeRestrictions

    val restrictions = ingredients.map {
        it.restrictions()
    }.filter {
        !it.empty()
    }

    return if (restrictions.isEmpty()) emptySized()
    else restrictions.reduce { a, b ->
        a.intersect(b)
    }
}

fun <T> SizedIterable<T>.intersect(that: SizedIterable<T>) =
        SizedCollection(this.toList().intersect(that.toList()))
