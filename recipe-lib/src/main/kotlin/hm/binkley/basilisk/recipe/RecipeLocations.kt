package hm.binkley.basilisk.recipe

import hm.binkley.basilisk.location.LocationRepository
import org.jetbrains.exposed.sql.Table

object RecipeLocationsRepository : Table("RECIPE_LOCATION") {
    val recipe =
            reference("recipe_code", RecipeRepository).primaryKey(0)
    val location =
            reference("location_code", LocationRepository).primaryKey(1)
}
