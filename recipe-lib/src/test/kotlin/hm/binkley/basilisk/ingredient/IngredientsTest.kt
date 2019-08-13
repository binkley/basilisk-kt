package hm.binkley.basilisk.ingredient

import ch.tutteli.atrium.api.cc.en_GB.containsExactly
import ch.tutteli.atrium.api.cc.en_GB.isA
import ch.tutteli.atrium.api.cc.en_GB.isEmpty
import ch.tutteli.atrium.api.cc.en_GB.toBe
import ch.tutteli.atrium.verbs.expect
import hm.binkley.basilisk.TestListener
import hm.binkley.basilisk.chef.ChefRecord
import hm.binkley.basilisk.chef.ChefResource
import hm.binkley.basilisk.chef.Chefs
import hm.binkley.basilisk.chef.Chefs.Companion.FIT
import hm.binkley.basilisk.db.testTransaction
import hm.binkley.basilisk.location.LocationResource
import hm.binkley.basilisk.location.Locations
import hm.binkley.basilisk.recipe.RecipeRecord
import hm.binkley.basilisk.recipe.RecipeResource
import hm.binkley.basilisk.recipe.RecipeStatus.PLANNING
import hm.binkley.basilisk.recipe.Recipes
import hm.binkley.basilisk.source.SourceRecord
import hm.binkley.basilisk.source.SourceResource
import hm.binkley.basilisk.source.Sources
import io.micronaut.test.annotation.MicronautTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import javax.inject.Inject

@MicronautTest
@TestInstance(PER_CLASS)
internal class IngredientsTest {
    companion object {
        const val name = "Rhubarb"
        const val code = "ING789"
    }

    @Inject
    lateinit var sources: Sources
    @Inject
    lateinit var locations: Locations
    @Inject
    lateinit var chefs: Chefs
    @Inject
    lateinit var recipes: Recipes
    @Inject
    lateinit var ingredients: Ingredients
    @Inject
    lateinit var listener: TestListener<IngredientSavedEvent>

    @AfterEach
    fun tearDown() {
        listener.reset()
    }

    @Test
    fun shouldFindNoIngredient() {
        testTransaction {
            val ingredient = ingredients.byCode(
                    code)

            expect(ingredient).toBe(null)
        }
    }

    @Test
    fun shouldFindUnusedIngredient() {
        val name = name
        val code = code

        testTransaction {
            val chef = chefs.new("CHEF BOB", "CHEF123")
            val source = sources.new(name, "SRC012")
            ingredients.newAny(source, code, chef, null)

            val ingredient = ingredients.byCode(code)!!

            expect(ingredient.code).toBe(code)
            expect(ingredient.name).toBe(name)
            expect(ingredient).isA<UnusedIngredient> { }
        }
    }

    @Test
    fun shouldFindUsedIngredient() {
        val name = name
        val code = code

        testTransaction {
            val chef = chefs.new("CHEF BOB", "CHEF123")
            val source = sources.new(name, "SRC012")
            val recipe = recipes.new("TASTY PIE", "REC456", chef)
            ingredients.newAny(source, code, chef, recipe)

            val ingredient = ingredients.byCode(code)!!

            expect(ingredient.code).toBe(code)
            expect(ingredient.name).toBe(name)
            expect(ingredient).isA<UsedIngredient> { }
        }
    }

    @Test
    fun shouldUnuseUsedIngredient() {
        val name = name
        val code = code

        testTransaction {
            val chef = chefs.new("CHEF BOB", "CHEF123")
            val source = sources.new(name, "SRC012")
            val recipe = recipes.new("TASTY PIE", "REC456", chef)
            ingredients.newAny(source, code, chef, recipe)

            val ingredient =
                    (ingredients.byCode(code)!! as UsedIngredient).unuse()

            expect(ingredient.code).toBe(code)
            expect(ingredient.name).toBe(name)
            expect(ingredient.recipe).toBe(null)
        }
    }

    @Test
    fun shouldUseUnusedIngredient() {
        val name = name
        val code = code

        testTransaction {
            val chef = ChefRecord.new {
                this.name = "CHEF BOB"
                this.code = "CHEF123"
                health = FIT
            }
            chef.flush()
            val source = SourceRecord.new {
                this.name = name
                this.code = "SRC012"
            }
            source.flush()
            val recipeRecord = RecipeRecord.new {
                this.name = "TASTY PIE"
                this.code = "REC456"
                this.chef = chef
                status = PLANNING
            }
            recipeRecord.flush()
            IngredientRecord.new {
                this.code = code
                this.chef = chef
                this.source = source
            }.flush()

            val recipe = recipes.from(recipeRecord)
            val ingredient =
                    (ingredients.byCode(code)!! as UnusedIngredient)
                            .use(recipe)

            expect(ingredient.code).toBe(code)
            expect(ingredient.name).toBe(name)
            expect(ingredient.recipe).toBe(recipe)
        }
    }

    @Test
    fun shouldPublishSaveEvents() {
        val sourceName = name
        val sourceCode = "SRC012"
        val chefName = "Chef Boy-ar-dee"
        val chefCode = "BOY"
        val locationName = "The Dallas Yellow Rose"
        val locationCode = "DAL"

        testTransaction {
            val source = sources.new(sourceName, sourceCode)
            val chef = chefs.new(chefName, chefCode)
            val recipe = recipes.new("TASTY PIE", "REC456", chef)
            val location = locations.new(locationName, locationCode)
            listener.reset()

            val firstSnapshot = IngredientResource(
                    SourceResource(source),
                    code, ChefResource(chef),
                    null,
                    listOf(LocationResource(location)))
            val secondSnapshot = IngredientResource(
                    firstSnapshot.source,
                    firstSnapshot.code, firstSnapshot.chef,
                    null,
                    listOf())
            val thirdSnapshot = IngredientResource(
                    secondSnapshot.source,
                    secondSnapshot.code, secondSnapshot.chef,
                    RecipeResource(recipe),
                    secondSnapshot.locations)

            val ingredient = ingredients.newUnused(
                    source, firstSnapshot.code, chef,
                    mutableListOf(location))

            listener.expectNext.containsExactly(IngredientSavedEvent(
                    null, ingredient))

            ingredient.update {
                this.locations.clear()
                save()
            }

            listener.expectNext.containsExactly(IngredientSavedEvent(
                    firstSnapshot, ingredient))

            val usedIngredient = ingredient.use(recipe)

            listener.expectNext.containsExactly(IngredientSavedEvent(
                    secondSnapshot, usedIngredient))

            val unusedIngredient = usedIngredient.unuse()

            listener.expectNext.containsExactly(IngredientSavedEvent(
                    thirdSnapshot, unusedIngredient))

            ingredient.update {
                delete()
            }

            listener.expectNext.containsExactly(IngredientSavedEvent(
                    secondSnapshot, null))
        }
    }

    @Test
    fun shouldSkipPublishSaveEventsIfUnchanged() {
        val sourceName = name
        val sourceCode = "SRC012"
        val chefName = "Chef Boy-ar-dee"
        val chefCode = "BOY"
        val locationName = "The Dallas Yellow Rose"
        val locationCode = "DAL"

        testTransaction {
            val source = sources.new(sourceName, sourceCode)
            val chef = chefs.new(chefName, chefCode)
            val location = locations.new(locationName, locationCode)
            listener.reset()

            val snapshot = IngredientResource(
                    SourceResource(source),
                    code, ChefResource(chef),
                    null,
                    listOf(LocationResource(location)))

            val ingredient = ingredients.newUnused(
                    source, snapshot.code, chef,
                    mutableListOf(location))

            listener.expectNext.containsExactly(IngredientSavedEvent(
                    null, ingredient))

            ingredient.update {
                save()
            }

            listener.expectNext.isEmpty()
        }
    }
}
