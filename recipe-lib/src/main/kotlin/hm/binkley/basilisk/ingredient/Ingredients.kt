package hm.binkley.basilisk.ingredient

import hm.binkley.basilisk.chef.ChefRecord
import hm.binkley.basilisk.chef.ChefRepository
import hm.binkley.basilisk.chef.Chefs
import hm.binkley.basilisk.db.findOne
import hm.binkley.basilisk.location.LocationRecord
import hm.binkley.basilisk.recipe.RecipeRecord
import hm.binkley.basilisk.recipe.RecipeRepository
import hm.binkley.basilisk.source.SourceRecord
import hm.binkley.basilisk.source.SourceRepository
import io.micronaut.context.event.ApplicationEvent
import io.micronaut.context.event.ApplicationEventPublisher
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable
import java.util.*
import javax.inject.Singleton

@Singleton
class Ingredients(
        private val chefs: Chefs,
        private val publisher: ApplicationEventPublisher) {
    fun byCode(code: String): Ingredient? {
        val record = IngredientRecord.findOne {
            IngredientRepository.code eq code
        }

        return when {
            null == record -> null
            null == record.recipe -> UnusedIngredient(record, this)
            else -> UsedIngredient(record, this)
        }
    }

    internal fun notifySaved(ingredient: Ingredient) {
        publisher.publishEvent(IngredientSavedEvent(ingredient))
    }

    internal fun chefFor(chefRecord: ChefRecord) = chefs.from(chefRecord)
}

interface IngredientDetails {
    val name: String
    val code: String
}

interface MutableIngredientDetails {
    val name: String // Not editable -- comes from Source
    var code: String
}

// TODO: before vs after
data class IngredientSavedEvent(val after: Ingredient) : ApplicationEvent(
        after)

sealed class Ingredient(
        private val record: IngredientRecord,
        private val factory: Ingredients)
    : IngredientDetails by record {
    val chef = factory.chefFor(record.chef)

    fun save(): Ingredient {
        record.flush()
        factory.notifySaved(this)
        return this
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Ingredient
        return record == other.record
    }

    override fun hashCode() = record.hashCode()

    override fun toString() =
            "${super.toString()}{record=$record, chef=$chef}"
}

class UnusedIngredient internal constructor(
        record: IngredientRecord,
        factory: Ingredients)
    : Ingredient(record, factory)

class UsedIngredient internal constructor(
        record: IngredientRecord,
        factory: Ingredients)
    : Ingredient(record, factory)

object IngredientRepository : IntIdTable("INGREDIENT") {
    val code = text("code")
    val chef = reference("chef_id", ChefRepository)
    val recipe = reference("recipe_id",
            RecipeRepository).nullable()
    // TODO: What to do about "source" hiding parent class member?
    val sourceRef = reference("source_id", SourceRepository)
}

class IngredientRecord(id: EntityID<Int>)
    : IntEntity(id),
        IngredientDetails,
        MutableIngredientDetails {
    companion object : IntEntityClass<IngredientRecord>(IngredientRepository)

    override val name
        get() = source.name
    override var code by IngredientRepository.code
    var chef by ChefRecord referencedOn IngredientRepository.chef
    var recipe by RecipeRecord optionalReferencedOn IngredientRepository.recipe
    var source by SourceRecord referencedOn IngredientRepository.sourceRef
    var locations by LocationRecord via IngredientLocationsRepository

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as IngredientRecord
        return code == other.code
                && chef == other.chef
                && recipe == other.recipe
                && source == other.source
                && locations == other.locations
    }

    override fun hashCode() =
            Objects.hash(code, chef, recipe, source, locations)

    override fun toString() =
            "${super.toString()}{id=$id, code=$code, chef=$chef, recipe=$recipe, source=$source, locations=$locations}"
}
