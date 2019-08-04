package hm.binkley.basilisk.recipe

import hm.binkley.basilisk.chef.Chef
import hm.binkley.basilisk.chef.ChefRecord
import hm.binkley.basilisk.chef.ChefRepository
import hm.binkley.basilisk.chef.Chefs
import hm.binkley.basilisk.db.findOne
import hm.binkley.basilisk.ingredient.IngredientRecord
import hm.binkley.basilisk.ingredient.IngredientRepository
import hm.binkley.basilisk.location.Location
import hm.binkley.basilisk.location.LocationRecord
import hm.binkley.basilisk.location.Locations
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
import javax.inject.Singleton

@Singleton
class Recipes(
        private val chefs: Chefs,
        private val locations: Locations,
        private val publisher: ApplicationEventPublisher) {
    fun byCode(code: String) = RecipeRecord.findOne {
        RecipeRepository.code eq code
    }?.let {
        from(it)
    }

    fun new(name: String, code: String, chef: Chef,
            locations: MutableList<Location> = mutableListOf()) =
            from(RecipeRecord.new {
                this.name = name
                this.code = code
                this.chef = recordFor(chef)
                this.status = PLANNING
            }).update(null) {
                // Exposed wants the record complete before adding relationships
                this.locations = locations
                save()
            }

    internal fun from(record: RecipeRecord) = Recipe(record, this)

    internal fun notifySaved(
            before: RecipeResource?,
            after: RecipeRecord?) {
        publisher.publishEvent(RecipeSavedEvent(
                before, after?.let { from(it) }))
    }

    internal fun chefFor(chefRecord: ChefRecord) =
            chefs.from(chefRecord)

    internal fun recordFor(chef: Chef) =
            chefs.toRecord(chef)

    internal fun locationFor(locationRecord: LocationRecord) =
            locations.from(locationRecord)

    internal fun recordFor(location: Location) =
            locations.toRecord(location)
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
        private val record: RecipeRecord,
        private val factory: Recipes)
    : RecipeDetails by record {
    val chef = factory.chefFor(record.chef)
    val locations: SizedIterable<Location>
        get() = record.locations.notForUpdate().mapLazy {
            factory.locationFor(it)
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

    override fun toString() = "${super.toString()}{record=$record}"
}

private class ListLike<T>(
        field: SizedIterable<T>,
        private val updateWith: (ListLike<T>) -> Unit)
    : AbstractMutableList<T>() {
    private val backing = field.toMutableList()

    private fun update() = updateWith(this)

    override val size: Int
        get() = backing.size

    override fun add(index: Int, element: T) {
        backing.add(index, element)
        update()
    }

    override fun get(index: Int) = backing.get(index)

    override fun removeAt(index: Int): T {
        val removeAt = backing.removeAt(index)
        update()
        return removeAt
    }

    override fun set(index: Int, element: T): T {
        val set = backing.set(index, element)
        update()
        return set
    }
}

class MutableRecipe internal constructor(
        private val snapshot: RecipeResource?,
        private val record: RecipeRecord,
        private val factory: Recipes) : MutableRecipeDetails by record {
    var chef: Chef
        get() = factory.chefFor(record.chef)
        set(update) {
            record.chef = factory.recordFor(update)
        }
    var locations: MutableList<Location>
        get() {
            val update = ListLike(record.locations.forUpdate().mapLazy {
                factory.locationFor(it)
            }, { update -> locations = update })
            locations = update // Glue changes of list back to record
            return update
        }
        set(update) {
            record.locations = SizedCollection(update.map {
                factory.recordFor(it)
            })
        }

    fun save() = apply {
        record.flush() // TODO: Aggressively flush, or wait for txn to end?
        factory.notifySaved(snapshot, record)
    }

    fun delete() {
        record.delete() // TODO: Detect if edited, not saved, then deleted
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
    val chef = reference("chef_id", ChefRepository)
    val status = enumerationByName("status", RecipeStatus.maxLength(),
            RecipeStatus::class)
}

class RecipeRecord(id: EntityID<Int>) : IntEntity(id),
        RecipeDetails,
        MutableRecipeDetails {
    companion object : IntEntityClass<RecipeRecord>(RecipeRepository)

    override var name by RecipeRepository.name
    override var code by RecipeRepository.code
    var chef by ChefRecord referencedOn RecipeRepository.chef
    override var status by RecipeRepository.status
    val ingredients by IngredientRecord optionalReferrersOn IngredientRepository.recipe
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
                && chef == other.chef
                && ingredients == other.ingredients
                && locations == other.locations
    }

    override fun hashCode() =
            Objects.hash(name, code, chef, ingredients, locations)

    override fun toString() =
            "${super.toString()}{id=$id, name=$name, code=$code, chef=$chef, ingredients=$ingredients, locations=$locations}"
}
