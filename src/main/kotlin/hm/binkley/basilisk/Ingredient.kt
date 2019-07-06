package hm.binkley.basilisk

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable

object IngredientRepository : IntIdTable("INGREDIENT") {
    val name = text("name")
    val chef = reference("chef_id", ChefRepository)
    val recipe = reference("recipe_id", RecipeRepository).nullable()
    // TODO: What to do about "source" hiding parent class member?
    val sourceRef = reference("source_id", SourceRepository)
}

class IngredientRecord(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<IngredientRecord>(IngredientRepository)

    var name by IngredientRepository.name
    var chef by ChefRecord referencedOn IngredientRepository.chef
    var recipe by RecipeRecord optionalReferencedOn IngredientRepository.recipe
    var source by SourceRecord referencedOn IngredientRepository.sourceRef
}
