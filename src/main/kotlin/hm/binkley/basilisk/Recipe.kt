package hm.binkley.basilisk

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable

object RecipeRepository : IntIdTable("RECIPE") {
    val name = text("name")
    val chef = reference("chef_id", ChefRepository)
}

class RecipeRecord(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<RecipeRecord>(RecipeRepository)

    var name by RecipeRepository.name
    var chef by ChefRecord referencedOn RecipeRepository.chef
    val ingredients by IngredientRecord optionalReferrersOn IngredientRepository.recipe
}
