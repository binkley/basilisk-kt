package hm.binkley.basilisk.recipe

import ch.tutteli.atrium.api.cc.en_GB.containsExactly
import ch.tutteli.atrium.api.cc.en_GB.isEmpty
import ch.tutteli.atrium.api.cc.en_GB.toBe
import ch.tutteli.atrium.verbs.expect
import hm.binkley.basilisk.TestListener
import hm.binkley.basilisk.chef.ChefResource
import hm.binkley.basilisk.chef.MockChefsClient
import hm.binkley.basilisk.chef.RemoteChefs
import hm.binkley.basilisk.db.testTransaction
import hm.binkley.basilisk.location.LocationResource
import hm.binkley.basilisk.location.PersistedLocations
import hm.binkley.basilisk.recipe.RecipeStatus.PLANNING
import hm.binkley.basilisk.recipe.RecipeStatus.PREPARING
import io.micronaut.test.annotation.MicronautTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import javax.inject.Inject

@MicronautTest
@TestInstance(PER_CLASS)
internal class RecipesTest {
    companion object {
        const val name = "TASTY PIE"
        const val code = "REC345"
    }

    @Inject
    lateinit var recipes: Recipes
    @Inject
    lateinit var mockChefsClient: MockChefsClient
    @Inject
    lateinit var chefs: RemoteChefs
    @Inject
    lateinit var locations: PersistedLocations
    @Inject
    lateinit var listener: TestListener<RecipeSavedEvent>

    @AfterEach
    fun tearDown() {
        listener.reset()
    }

    @Test
    fun shouldFindNoRecipe() {
        testTransaction {
            val ingredient = recipes.byCode(code)

            expect(ingredient).toBe(null)
        }
    }

    @Test
    fun shouldRoundTrip() {
        val chefName = "Chef Boy-ar-dee"
        val chefCode = "BOY"

        testTransaction {
            val chef = chefs.new(ChefResource(chefName, chefCode))

            recipes.new(name, code, chef)
            val recipe = recipes.byCode(code)!!

            expect(recipe.code).toBe(code)
            expect(recipe.name).toBe(name)
        }
    }

    @Test
    fun shouldPublishSaveEvents() {
        val chefName = "Chef Boy-ar-dee"
        val chefCode = "BOY"
        val locationName = "The Dallas Yellow Rose"
        val locationCode = "DAL"

        testTransaction {
            val chef = chefs.new(ChefResource(chefName, chefCode))
            val location = locations.new(
                    LocationResource(locationName, locationCode))
            listener.reset()

            val firstSnapshot = RecipeResource(
                    name, code, ChefResource(chef), PLANNING,
                    listOf(LocationResource(location)))
            val secondSnapshot = RecipeResource(
                    "COOL CAKE", code, ChefResource(chef), PLANNING,
                    listOf())
            val thirdSnapshot = RecipeResource(
                    "COOL CAKE", code, ChefResource(chef), PREPARING,
                    listOf(LocationResource(location)))

            val recipe = recipes.new(
                    firstSnapshot.name, firstSnapshot.code, chef,
                    mutableListOf(location))

            listener.expectNext.containsExactly(RecipeSavedEvent(
                    null, recipe))

            recipe.update {
                this.name = secondSnapshot.name
                this.locations.clear()
                save()
            }

            listener.expectNext.containsExactly(RecipeSavedEvent(
                    firstSnapshot, recipe))

            recipe.update {
                this.status = PREPARING
                this.locations = mutableListOf(location) // Change our minds
                save()
            }

            listener.expectNext.containsExactly(RecipeSavedEvent(
                    secondSnapshot, recipe))

            recipe.update {
                delete()
            }

            listener.expectNext.containsExactly(RecipeSavedEvent(
                    thirdSnapshot, null))
        }
    }

    @Test
    fun shouldSkipPublishSaveEventsIfUnchanged() {
        val chefName = "Chef Boy-ar-dee"
        val chefCode = "BOY"
        val locationName = "The Dallas Yellow Rose"
        val locationCode = "DAL"

        testTransaction {
            val chef = chefs.new(ChefResource(chefName, chefCode))
            val location = locations.new(
                    LocationResource(locationName, locationCode))
            listener.reset()

            val snapshot = RecipeResource(
                    name, code, ChefResource(chef), PLANNING,
                    listOf(LocationResource(location)))

            val recipe = recipes.new(
                    snapshot.name, snapshot.code, chef,
                    mutableListOf(location))

            listener.expectNext.containsExactly(RecipeSavedEvent(
                    null, recipe))

            recipe.update {
                save()
            }

            listener.expectNext.isEmpty()
        }
    }
}
