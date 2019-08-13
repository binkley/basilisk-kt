package hm.binkley.basilisk.ingredient

import hm.binkley.basilisk.location.LocationRepository
import org.jetbrains.exposed.sql.Table

object IngredientLocationsRepository : Table("INGREDIENT_LOCATION") {
    val ingredient =
            reference("ingredient_id", IngredientRepository).primaryKey(0)
    val location =
            reference("location_id", LocationRepository).primaryKey(1)
}
