package hm.binkley.basilisk.ingredient

import hm.binkley.basilisk.chef.Chef
import hm.binkley.basilisk.chef.ChefRecord
import hm.binkley.basilisk.chef.ChefRepository
import hm.binkley.basilisk.chef.Chefs
import hm.binkley.basilisk.db.ListLike
import hm.binkley.basilisk.db.findOne
import hm.binkley.basilisk.domain.notifySaved
import hm.binkley.basilisk.location.Location
import hm.binkley.basilisk.location.LocationRecord
import hm.binkley.basilisk.location.Locations
import hm.binkley.basilisk.recipe.Recipe
import hm.binkley.basilisk.recipe.RecipeRecord
import hm.binkley.basilisk.recipe.RecipeRepository
import hm.binkley.basilisk.recipe.Recipes
import hm.binkley.basilisk.source.Source
import hm.binkley.basilisk.source.SourceRecord
import hm.binkley.basilisk.source.SourceRepository
import hm.binkley.basilisk.source.Sources
import io.micronaut.context.event.ApplicationEvent
import io.micronaut.context.event.ApplicationEventPublisher
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.sql.SizedCollection
import org.jetbrains.exposed.sql.SizedIterable
import org.jetbrains.exposed.sql.mapLazy
import java.util.*
import javax.inject.Singleton

@Singleton
class Ingredients(
        private val sources: Sources,
        private val chefs: Chefs,
        private val recipes: Recipes,
        private val locations: Locations,
        private val publisher: ApplicationEventPublisher) {
    fun byCode(code: String) = IngredientRecord.findOne {
        IngredientRepository.code eq code
    }?.let {
        from(it)
    }

    private fun <I : Ingredient<*>> new(
            source: Source, code: String, chef: Chef,
            locations: MutableList<Location> = mutableListOf(),
            block: (IngredientRecord, Ingredients) -> I) =
            block(IngredientRecord.new {
                this.source = toRecord(source)
                this.code = code
                this.chef = toRecord(chef)
            }, this).update(null) {
                this.locations = locations
                save()
            } as I

    fun newUnused(source: Source, code: String, chef: Chef,
                  locations: MutableList<Location> = mutableListOf()) =
            new(source, code, chef, locations) { record, factory ->
                record.recipe = null // Explicit, even if redundant
                UnusedIngredient(record, factory)
            }

    fun newUsed(source: Source, code: String, chef: Chef, recipe: Recipe,
                locations: MutableList<Location> = mutableListOf()) =
            new(source, code, chef, locations) { record, factory ->
                record.recipe = factory.toRecord(recipe)
                UsedIngredient(record, factory)
            }

    fun newAny(source: Source, code: String, chef: Chef, recipe: Recipe?,
               locations: MutableList<Location> = mutableListOf()) =
            new(source, code, chef, locations) { record, factory ->
                record.recipe = recipe?.let { factory.toRecord(recipe) }
                factory.from(record)
            }

    /** For implementors of other record types having a reference. */
    fun from(record: IngredientRecord) =
            if (null == record.recipe) UnusedIngredient(record, this)
            else UsedIngredient(record, this)

    internal fun notifySaved(
            before: IngredientResource?, after: IngredientRecord?) =
            notifySaved(before, after?.let { from(it) }, publisher,
                    ::IngredientResource, ::IngredientSavedEvent)

    internal fun sourceFrom(sourceRecord: SourceRecord) =
            sources.from(sourceRecord)

    internal fun toRecord(source: Source) = sources.toRecord(source)

    internal fun chefFrom(chefRecord: ChefRecord) =
            chefs.from(chefRecord)

    internal fun toRecord(chef: Chef) =
            chefs.toRecord(chef)

    internal fun recipeFrom(recipeRecord: RecipeRecord) =
            recipes.from(recipeRecord)

    internal fun toRecord(recipe: Recipe) =
            recipes.toRecord(recipe)

    internal fun locationFrom(locationRecord: LocationRecord) =
            locations.from(locationRecord)

    internal fun toRecord(location: Location) =
            locations.toRecord(location)
}

interface IngredientDetails {
    val name: String
    val code: String
}

interface MutableIngredientDetails {
    val name: String // Not editable -- comes from Source
    var code: String
}

data class IngredientSavedEvent(
        val before: IngredientResource?,
        val after: Ingredient<*>?) : ApplicationEvent(after ?: before)

sealed class Ingredient<I : Ingredient<I>>(
        internal val record: IngredientRecord,
        internal val factory: Ingredients)
    : IngredientDetails by record {
    val source
        get() = factory.sourceFrom(record.source)
    val chef
        get() = factory.chefFrom(record.chef)
    val recipe: Recipe?
        get() {
            val recipeRecord = record.recipe
            return recipeRecord?.let { factory.recipeFrom(recipeRecord) }
        }
    val locations: SizedIterable<Location>
        get() = record.locations.notForUpdate().mapLazy {
            factory.locationFrom(it)
        }

    fun update(block: MutableIngredient.() -> Unit): I =
            update(IngredientResource(this), block)

    internal fun update(
            snapshot: IngredientResource?,
            block: MutableIngredient.() -> Unit): I {
        MutableIngredient(snapshot, record, factory).block()
        return this as I
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Ingredient<*>
        return record == other.record
    }

    override fun hashCode() = record.hashCode()

    override fun toString() =
            "${super.toString()}{record=$record, chef=$chef}"
}

class UnusedIngredient internal constructor(
        record: IngredientRecord,
        factory: Ingredients)
    : Ingredient<UnusedIngredient>(record, factory) {
    fun use(recipe: Recipe): UsedIngredient {
        update {
            this.recipe = recipe
            save()
        }
        return UsedIngredient(record, factory)
    }
}

class UsedIngredient internal constructor(
        record: IngredientRecord,
        factory: Ingredients)
    : Ingredient<UsedIngredient>(record, factory) {
    fun unuse(): UnusedIngredient {
        update {
            recipe = null
            save()
        }
        return UnusedIngredient(record, factory)
    }
}

class MutableIngredient internal constructor(
        private val snapshot: IngredientResource?,
        private val record: IngredientRecord,
        private val factory: Ingredients) : MutableIngredientDetails by record {
    val source: Source // Immutable on creation
        get() = factory.sourceFrom(record.source)
    val chef: Chef // Immutable on creation
        get() = factory.chefFrom(record.chef)
    var recipe: Recipe?
        get() {
            val recipeRecord = record.recipe
            return recipeRecord?.let { factory.recipeFrom(recipeRecord) }
        }
        set(update) {
            record.recipe = update?.let { factory.toRecord(update) }
        }
    var locations: MutableList<Location>
        get() {
            val update = ListLike(
                    record.locations.forUpdate().mapLazy {
                        factory.locationFrom(it)
                    }, { update -> locations = update })
            locations = update // Glue changes of list back to record
            return update
        }
        set(update) {
            record.locations = SizedCollection(update.map {
                factory.toRecord(it)
            })
        }

    fun save() = apply {
        record.flush() // TODO: Aggressively flush, or wait for txn to end?
        factory.notifySaved(snapshot, record)
    }

    fun delete() {
        record.delete() // TODO: Detect if edited, and not saved, then deleted
        factory.notifySaved(snapshot, null)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as MutableIngredient
        return snapshot == other.snapshot
                && record == other.record
    }

    override fun hashCode() = Objects.hash(snapshot, record)

    override fun toString() =
            "${super.toString()}{snapshot=$snapshot, record=$record}"
}

object IngredientRepository : IntIdTable("INGREDIENT") {
    // TODO: What to do about "source" hiding parent class member?
    val sourceRef = reference("source_id", SourceRepository)
    val code = text("code")
    val chef = reference("chef_id", ChefRepository)
    val recipe = reference("recipe_id",
            RecipeRepository).nullable()
}

class IngredientRecord(id: EntityID<Int>)
    : IntEntity(id),
        IngredientDetails,
        MutableIngredientDetails {
    companion object : IntEntityClass<IngredientRecord>(IngredientRepository)

    var source by SourceRecord referencedOn IngredientRepository.sourceRef
    override val name
        get() = source.name
    override var code by IngredientRepository.code
    var chef by ChefRecord referencedOn IngredientRepository.chef
    var recipe by RecipeRecord optionalReferencedOn IngredientRepository.recipe
    var locations by LocationRecord via IngredientLocationsRepository

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as IngredientRecord
        return source == other.source
                && code == other.code
                && chef == other.chef
                && recipe == other.recipe
                && locations == other.locations
    }

    override fun hashCode() =
            Objects.hash(source, code, chef, recipe, locations)

    override fun toString() =
            "${super.toString()}{id=$id, source=$source, code=$code, chef=$chef, recipe=$recipe, locations=$locations}"
}
