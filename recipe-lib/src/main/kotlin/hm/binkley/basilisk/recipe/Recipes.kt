package hm.binkley.basilisk.recipe

import hm.binkley.basilisk.chef.RemoteChef
import hm.binkley.basilisk.chef.RemoteChefs
import hm.binkley.basilisk.db.asList
import hm.binkley.basilisk.db.findOne
import hm.binkley.basilisk.domain.notifySaved
import hm.binkley.basilisk.ingredient.Ingredients
import hm.binkley.basilisk.ingredient.UsedIngredient
import hm.binkley.basilisk.location.LocationRecord
import hm.binkley.basilisk.location.PersistedLocation
import hm.binkley.basilisk.location.PersistedLocations
import hm.binkley.basilisk.recipe.RecipeStatus.PLANNING
import io.micronaut.context.event.ApplicationEvent
import io.micronaut.context.event.ApplicationEventPublisher
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.sql.SizedCollection
import org.jetbrains.exposed.sql.SizedIterable
import org.jetbrains.exposed.sql.emptySized
import org.jetbrains.exposed.sql.mapLazy
import java.util.*
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class Recipes(
        private val chefs: RemoteChefs,
        private val ingredientsFactory: Provider<Ingredients>, // Circular
        private val locations: PersistedLocations,
        private val publisher: ApplicationEventPublisher) {
    fun byCode(code: String) = RecipeRecord.findOne {
        RecipeRepository.code eq code
    }?.let {
        from(it)
    }

    /** Saves a new recipe in [PLANNING] status. */
    fun new(name: String, code: String, chef: RemoteChef,
            locations: MutableList<PersistedLocation> = mutableListOf()) =
            from(RecipeRecord.new {
                this.name = name
                this.code = code
                this.chefCode = chef.code
                this.status = PLANNING
            }).update(null) {
                // Exposed wants the record complete before adding relationships
                this.locations = locations
                save()
            }

    /** For implementors of other record types having a reference. */
    fun from(record: RecipeRecord) = Recipe(record, this)

    /** For implementors of other record types having a reference. */
    fun toRecord(recipe: Recipe) = recipe.record

    internal fun notifySaved(before: RecipeResource?, after: RecipeRecord?) =
            notifySaved(before, after?.let { from(it) }, publisher,
                    ::RecipeResource, ::RecipeSavedEvent)

    internal fun chefFrom(chefCode: String) =
            chefs.byCode(chefCode)!! // TODO: What about remote failure?

    internal fun locationFrom(locationRecord: LocationRecord) =
            locations.from(locationRecord)

    internal fun toRecord(location: PersistedLocation) =
            locations.toRecord(location)

    internal fun ingredientsFrom(recipe: Recipe) =
            ingredientsFactory.get().byRecipe(recipe)
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
    val status: RecipeStatus
}

interface MutableRecipeDetails {
    var name: String
    var code: String
    var status: RecipeStatus
}

data class RecipeSavedEvent(
        val before: RecipeResource?,
        val after: Recipe?) : ApplicationEvent(after ?: before)

class Recipe internal constructor(
        internal val record: RecipeRecord,
        private val factory: Recipes)
    : RecipeDetails by record {
    val chef
        get() = factory.chefFrom(record.chefCode)
    val ingredients: SizedIterable<UsedIngredient>
        get() = factory.ingredientsFrom(this)
    val locations: SizedIterable<PersistedLocation>
        get() = record.locations.notForUpdate().mapLazy {
            factory.locationFrom(it)
        }

    fun update(block: MutableRecipe.() -> Unit) =
            update(RecipeResource(this), block)

    internal inline fun update(
            snapshot: RecipeResource?,
            block: MutableRecipe.() -> Unit) = apply {
        MutableRecipe(snapshot, record, factory).block()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Recipe

        return record == other.record
    }

    override fun hashCode() = record.hashCode()

    override fun toString() =
            "${super.toString()}{record=$record, chef=$chef, location=$locations}"
}

class MutableRecipe internal constructor(
        private val snapshot: RecipeResource?,
        private val record: RecipeRecord,
        private val factory: Recipes) : MutableRecipeDetails by record {
    var chef: RemoteChef // TODO: OOPS!  Use interface, not concrete
        get() = factory.chefFrom(record.chefCode)
        set(update) {
            record.chefCode = update.code
        }
    var locations: MutableList<PersistedLocation>
        get() {
            val update = record.locations.forUpdate().mapLazy {
                factory.locationFrom(it)
            }.asList { update ->
                locations = update
            }
            locations = update // Glue changes of list back to record
            return update
        }
        set(update) {
            record.locations = SizedCollection(update.map {
                factory.toRecord(it)
            })
        }

    fun save() = apply {
        record.flush()
        factory.notifySaved(snapshot, record)
    }

    fun delete() {
        record.delete()
        factory.notifySaved(snapshot, null)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as MutableRecipe
        return snapshot == other.snapshot
                && record == other.record
    }

    override fun hashCode() = Objects.hash(snapshot, record)

    override fun toString() =
            "${super.toString()}{snapshot=$snapshot, record=$record}"
}

object RecipeRepository : IntIdTable("RECIPE") {
    val name = text("name")
    val code = text("code")
    val chefCode = text("chef_code")
    val status = enumerationByName("status", RecipeStatus.maxLength(),
            RecipeStatus::class)
}

class RecipeRecord(id: EntityID<Int>) : IntEntity(id),
        RecipeDetails,
        MutableRecipeDetails {
    companion object : IntEntityClass<RecipeRecord>(RecipeRepository)

    override var name by RecipeRepository.name
    override var code by RecipeRepository.code
    var chefCode by RecipeRepository.chefCode
    override var status by RecipeRepository.status
    var locations by LocationRecord via RecipeLocationsRepository

    override fun delete() {
        locations = emptySized()
        super.delete()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as RecipeRecord
        return name == other.name
                && code == other.code
                && chefCode == other.chefCode
                && locations == other.locations
    }

    override fun hashCode() =
            Objects.hash(name, code, chefCode, locations)

    override fun toString() =
            "${super.toString()}{id=$id, name=$name, code=$code, chefCode=$chefCode, locations=$locations}"
}
