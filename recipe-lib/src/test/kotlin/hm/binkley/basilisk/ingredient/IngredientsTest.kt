package hm.binkley.basilisk.ingredient

import ch.tutteli.atrium.api.cc.en_GB.containsExactly
import ch.tutteli.atrium.api.cc.en_GB.isA
import ch.tutteli.atrium.api.cc.en_GB.isEmpty
import ch.tutteli.atrium.api.cc.en_GB.toBe
import ch.tutteli.atrium.verbs.expect
import hm.binkley.basilisk.TestListener
import hm.binkley.basilisk.chef.ChefResource
import hm.binkley.basilisk.chef.MockChefsClient
import hm.binkley.basilisk.chef.RemoteChefs
import hm.binkley.basilisk.db.asList
import hm.binkley.basilisk.db.testTransaction
import hm.binkley.basilisk.location.LocationResource
import hm.binkley.basilisk.location.PersistedLocations
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
        const val code = "ING789"
        const val name = "Rhubarb"
    }

    @Inject
    lateinit var sources: Sources
    @Inject
    lateinit var locations: PersistedLocations
    @Inject
    lateinit var mockChefsClient: MockChefsClient
    @Inject
    lateinit var chefs: RemoteChefs
    @Inject
    lateinit var recipes: Recipes
    @Inject
    lateinit var ingredients: Ingredients
    @Inject
    lateinit var listener: TestListener<IngredientChangedEvent>

    @AfterEach
    fun tearDown() {
        listener.reset()
    }

    @Test
    fun shouldFindNoIngredients() {
        testTransaction {
            expect(ingredients.all()).isEmpty()

            val found = ingredients.byCode(code)

            expect(found).toBe(null)
        }
    }

    @Test
    fun shouldFindUnusedIngredient() {
        val code = code
        val name = name

        testTransaction {
            val chef = chefs.new(ChefResource("CHEF123", "CHEF BOB"))
            val source = sources.new("SRC012", name)
            val recipe = recipes.new("REC456", "TASTY PIE", chef)
            ingredients.newAny(source, code, chef, null)

            val ingredient = ingredients.byCode(code)!!

            expect(ingredient.code).toBe(code)
            expect(ingredient.name).toBe(name)
            expect(ingredient).isA<UnusedIngredient> { }

            expect(ingredients.byRecipe(recipe).asList()).isEmpty()
        }
    }

    @Test
    fun shouldFindUsedIngredient() {
        val code = code
        val name = name

        testTransaction {
            val chef = chefs.new(ChefResource("CHEF123", "CHEF BOB"))
            val source = sources.new("SRC012", name)
            val recipe = recipes.new("REC456", "TASTY PIE", chef)
            ingredients.newAny(source, code, chef, recipe)

            val ingredient = ingredients.byCode(code)!!

            expect(ingredient.code).toBe(code)
            expect(ingredient.name).toBe(name)
            expect(ingredient).isA<UsedIngredient> { }

            expect(ingredients.byRecipe(recipe).asList())
                    .containsExactly(ingredient)
        }
    }

    @Test
    fun shouldUnuseUsedIngredient() {
        val code = code
        val name = name

        testTransaction {
            val chef = chefs.new(ChefResource("CHEF123", "CHEF BOB"))
            val source = sources.new("SRC012", name)
            val recipe = recipes.new("REC456", "TASTY PIE", chef)
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
        val code = code
        val name = name

        testTransaction {
            val chef = ChefResource("CHEF123", "CHEF BOB")
            mockChefsClient.one = chef
            val source = SourceRecord.new {
                this.code = "SRC012"
                this.name = name
            }
            source.flush()
            val recipeRecord = RecipeRecord.new {
                this.code = "REC456"
                this.name = "TASTY PIE"
                this.chefCode = chef.code
                status = PLANNING
            }
            recipeRecord.flush()
            IngredientRecord.new {
                this.code = code
                this.chefCode = chef.code
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
        val sourceCode = "SRC012"
        val sourceName = name
        val chefCode = "BOY"
        val chefName = "Chef Boy-ar-dee"
        val locationCode = "DAL"
        val locationName = "The Dallas Yellow Rose"

        testTransaction {
            val source = sources.new(sourceCode, sourceName)
            val chef = chefs.new(ChefResource(chefCode, chefName))
            val recipe = recipes.new("REC456", "TASTY PIE", chef)
            val location = locations.new(
                    LocationResource(locationCode, locationName))
            listener.reset()

            val firstSnapshot = IngredientResource(
                    SourceResource(source), code, name, ChefResource(chef),
                    null,
                    listOf(LocationResource(location)))
            val secondSnapshot = IngredientResource(
                    firstSnapshot.source,
                    firstSnapshot.code, firstSnapshot.name,
                    firstSnapshot.chef, null, listOf())
            val thirdSnapshot = IngredientResource(
                    secondSnapshot.source,
                    secondSnapshot.code, firstSnapshot.name,
                    secondSnapshot.chef, RecipeResource(recipe),
                    secondSnapshot.locations)

            val ingredient = ingredients.newUnused(
                    source, firstSnapshot.code, chef,
                    mutableListOf(location))

            listener.expectNext.containsExactly(IngredientChangedEvent(
                    null, IngredientResource(ingredient)))

            ingredient.update {
                this.locations.clear()
                save()
            }

            listener.expectNext.containsExactly(IngredientChangedEvent(
                    firstSnapshot, IngredientResource(ingredient)))

            val usedIngredient = ingredient.use(recipe)

            listener.expectNext.containsExactly(IngredientChangedEvent(
                    secondSnapshot, IngredientResource(usedIngredient)))

            val unusedIngredient = usedIngredient.unuse()

            listener.expectNext.containsExactly(IngredientChangedEvent(
                    thirdSnapshot, IngredientResource(unusedIngredient)))

            ingredient.update {
                delete()
            }

            listener.expectNext.containsExactly(IngredientChangedEvent(
                    secondSnapshot, null))
        }
    }

    @Test
    fun shouldSkipPublishSaveEventsIfUnchanged() {
        val sourceCode = "SRC012"
        val sourceName = name
        val chefCode = "BOY"
        val chefName = "Chef Boy-ar-dee"
        val locationCode = "DAL"
        val locationName = "The Dallas Yellow Rose"

        testTransaction {
            val source = sources.new(sourceCode, sourceName)
            val chef = chefs.new(ChefResource(chefCode, chefName))
            val location = locations.new(
                    LocationResource(locationCode, locationName))
            listener.reset()

            val snapshot = IngredientResource(
                    SourceResource(source), code, name, ChefResource(chef),
                    null,
                    listOf(LocationResource(location)))

            val ingredient = ingredients.newUnused(
                    source, snapshot.code, chef,
                    mutableListOf(location))

            listener.expectNext.containsExactly(IngredientChangedEvent(
                    null, IngredientResource(ingredient)))

            ingredient.update {
                save()
            }

            listener.expectNext.isEmpty()
        }
    }
}
