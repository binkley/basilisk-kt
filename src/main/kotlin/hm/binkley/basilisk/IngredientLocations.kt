package hm.binkley.basilisk

import org.jetbrains.exposed.sql.Table

object IngredientLocationsRepository : Table("INGREDIENT_LOCATION") {
    val ingredient = reference("ingredient_id", SourceRepository).primaryKey(0)
    val location = reference("location_id", LocationRepository).primaryKey(1)
}
