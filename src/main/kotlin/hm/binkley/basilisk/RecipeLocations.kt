package hm.binkley.basilisk

import org.jetbrains.exposed.sql.Table

object RecipeLocationsRepository : Table("RECIPE_LOCATION") {
    val recipe = reference("recipe_id", SourceRepository).primaryKey(0)
    val location = reference("location_id", LocationRepository).primaryKey(1)
}
