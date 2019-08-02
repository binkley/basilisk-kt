package hm.binkley.basilisk.recipe

import hm.binkley.basilisk.chef.ChefRecord
import hm.binkley.basilisk.chef.ChefRepository
import hm.binkley.basilisk.db.findOne
import hm.binkley.basilisk.ingredient.IngredientRecord
import hm.binkley.basilisk.ingredient.IngredientRepository
import hm.binkley.basilisk.location.Location
import hm.binkley.basilisk.location.LocationRecord
import hm.binkley.basilisk.location.Locations
import io.micronaut.context.event.ApplicationEvent
import io.micronaut.context.event.ApplicationEventPublisher
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.sql.SizedIterable
import org.jetbrains.exposed.sql.mapLazy
import java.util.*
import javax.inject.Singleton

@Singleton
class Recipes(
        private val locations: Locations,
        private val publisher: ApplicationEventPublisher) {
    fun recipe(code: String) = RecipeRecord.findOne {
        RecipeRepository.code eq code
    }?.let {
        recipe(it)
    }

    fun recipe(record: RecipeRecord) = Recipe(record, this)

    fun create(name: String, code: String) = Recipe(RecipeRecord.new {
        this.name = name
        this.code = code
    }, this).save()

    internal fun notifySaved(recipe: Recipe) {
        publisher.publishEvent(RecipeSavedEvent(recipe))
    }

    internal fun locationFor(locationRecord: LocationRecord) =
            locations.location(locationRecord)

    override fun toString() =
            "${super.toString()}{locations=$locations, publisher=$publisher}"
}

enum class RecipeStatus {
    PLANNING, PREPARING, SERVED;

    companion object {
        fun maxLength() = values().map {
            it.name.length
        }.max() ?: 0
    }
}

interface RecipeDetails {
    val name: String
    val code: String
}

data class RecipeSavedEvent(val after: Recipe) : ApplicationEvent(after)

class Recipe(
        private val record: RecipeRecord,
        private val factory: Recipes)
    : RecipeDetails by record {
    val locations: SizedIterable<Location>
        get() = record.locations.mapLazy {
            factory.locationFor(it)
        }

    fun save(): Recipe {
        record.flush()
        factory.notifySaved(this)
        return this
    }

    override fun toString() = "${super.toString()}{record=$record}"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Recipe

        return record == other.record
    }

    override fun hashCode() = record.hashCode()
}

object RecipeRepository : IntIdTable("RECIPE") {
    val name = text("name")
    val code = text("code")
    val chef = reference("chef_id", ChefRepository)
    val status = enumerationByName("status", RecipeStatus.maxLength(),
            RecipeStatus::class)
}

class RecipeRecord(id: EntityID<Int>) : IntEntity(id),
        RecipeDetails {
    companion object : IntEntityClass<RecipeRecord>(RecipeRepository)

    override var name by RecipeRepository.name
    override var code by RecipeRepository.code
    var chef by ChefRecord referencedOn RecipeRepository.chef
    var status by RecipeRepository.status
    val ingredients by IngredientRecord optionalReferrersOn IngredientRepository.recipe
    var locations by LocationRecord via RecipeLocationsRepository

    override fun toString() =
            "${super.toString()}{id=$id, name=$name, code=$code, chef=$chef, ingredients=$ingredients, locations=$locations}"

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

    override fun hashCode() =
            Objects.hash(name, code, chef, ingredients, locations)
}
