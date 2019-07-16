package hm.binkley.basilisk

import io.micronaut.context.event.ApplicationEvent
import io.micronaut.context.event.ApplicationEventPublisher
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable
import javax.inject.Singleton

@Singleton
class Ingredients(
        private val chefs: Chefs,
        private val publisher: ApplicationEventPublisher) {
    fun ingredient(code: String): Ingredient? {
        val record = IngredientRecord.findOne {
            IngredientRepository.code eq code
        }

        return if (null == record) null
        else if (null == record.recipe) UnusedIngredient(record, chefs, publisher)
        else UsedIngredient(record, chefs, publisher)
    }
}

interface IngredientRecordData {
    val name: String
    val code: String
}

data class IngredientSavedEvent(val ingredient: Ingredient)
    : ApplicationEvent(ingredient)

sealed class Ingredient(
        private val record: IngredientRecord,
        chefs: Chefs,
        private val publisher: ApplicationEventPublisher)
    : IngredientRecordData by record {
    val chef = chefs.chef(record.chef)

    fun save(): Ingredient {
        record.flush()
        publisher.publishEvent(IngredientSavedEvent(this))
        return this
    }
}

class UnusedIngredient(
        record: IngredientRecord,
        chefs: Chefs,
        publisher: ApplicationEventPublisher)
    : Ingredient(record, chefs, publisher)

class UsedIngredient(
        record: IngredientRecord,
        chefs: Chefs,
        publisher: ApplicationEventPublisher)
    : Ingredient(record, chefs, publisher)

object IngredientRepository : IntIdTable("INGREDIENT") {
    val code = text("code")
    val chef = reference("chef_id", ChefRepository)
    val recipe = reference("recipe_id", RecipeRepository).nullable()
    // TODO: What to do about "source" hiding parent class member?
    val sourceRef = reference("source_id", SourceRepository)
}

class IngredientRecord(id: EntityID<Int>) : IntEntity(id), IngredientRecordData {
    companion object : IntEntityClass<IngredientRecord>(IngredientRepository)

    override val name
        get() = source.name
    override var code by IngredientRepository.code
    var chef by ChefRecord referencedOn IngredientRepository.chef
    var recipe by RecipeRecord optionalReferencedOn IngredientRepository.recipe
    var source by SourceRecord referencedOn IngredientRepository.sourceRef
    var locations by LocationRecord via IngredientLocationsRepository
}
