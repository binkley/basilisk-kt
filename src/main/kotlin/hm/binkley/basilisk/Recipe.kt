package hm.binkley.basilisk

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable
import java.util.*

object RecipeRepository : IntIdTable("RECIPE") {
    val name = text("name")
    val code = text("code")
    val chef = reference("chef_id", ChefRepository)
}

class RecipeRecord(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<RecipeRecord>(RecipeRepository)

    var name by RecipeRepository.name
    var code by RecipeRepository.code
    var chef by ChefRecord referencedOn RecipeRepository.chef
    val ingredients by IngredientRecord optionalReferrersOn IngredientRepository.recipe
    var locations by LocationRecord via RecipeLocationsRepository

    override fun toString(): String {
        return "${super.toString()}{id=$id, name=$name, code=$code, chef=$chef, ingredients=$ingredients, locations=$locations}"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as RecipeRecord
        return name == other.name
                && code == other.code
                && chef == other.chef
                && ingredients == other.ingredients
                && locations == other.locations
    }

    override fun hashCode(): Int {
        return Objects.hash(name, code, chef, ingredients, locations)
    }
}
