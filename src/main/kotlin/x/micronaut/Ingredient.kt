package x.micronaut

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable

class Ingredient(private val record: IngredientRecord) {
    val name
        get() = record.name
}

object IngredientRepository : IntIdTable("INGREDIENT") {
    val name = text("name")
    val chef = reference("CHEF_ID", ChefRepository)
    val recipe = reference("RECIPE_ID", RecipeRepository).nullable()
    // TODO: What to do about "source" hiding parent class member?
    val sourceRef = reference("SOURCE_ID", SourceRepository)
}

class IngredientRecord(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<IngredientRecord>(IngredientRepository)

    var name by IngredientRepository.name
    var chef by ChefRecord referencedOn IngredientRepository.chef
    var recipe by RecipeRecord optionalReferencedOn IngredientRepository.recipe
    var source by SourceRecord referencedOn IngredientRepository.sourceRef
}
