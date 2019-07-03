package x.micronaut

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable

class Recipe(private val record: RecipeRecord) {
    val name
        get() = record.name
}

object RecipeRepository : IntIdTable("RECIPE") {
    val name = text("name")
    val chef = reference("CHEF_ID", ChefRepository)
}

class RecipeRecord(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<RecipeRecord>(RecipeRepository)

    var name by RecipeRepository.name
    var chef by ChefRecord referencedOn RecipeRepository.chef
}
