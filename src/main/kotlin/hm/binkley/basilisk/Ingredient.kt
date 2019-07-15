package hm.binkley.basilisk

import io.micronaut.context.event.ApplicationEvent
import io.micronaut.context.event.ApplicationEventPublisher
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Ingredients(@Inject private val publisher: ApplicationEventPublisher) {
    // TODO: Throw or return null when ID not found?
    fun ingredient(id: Int): Ingredient {
        val found = IngredientRecord.findById(id)
        return Ingredient(found!!, publisher)
    }
}

interface IngredientRecordData {
    val name: String
    val code: String
}

data class IngredientSavedEvent(val ingredient: Ingredient)
    : ApplicationEvent(ingredient)

class Ingredient(
        private val record: IngredientRecord,
        private val publisher: ApplicationEventPublisher)
    : IngredientRecordData by record {
    fun save(): Ingredient {
        record.flush()
        publisher.publishEvent(IngredientSavedEvent(this))
        return this
    }
}

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
