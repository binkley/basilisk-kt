package hm.binkley.basilisk.ingredient

import ch.tutteli.atrium.api.cc.en_GB.containsExactly
import ch.tutteli.atrium.api.cc.en_GB.isA
import ch.tutteli.atrium.api.cc.en_GB.toBe
import ch.tutteli.atrium.verbs.expect
import hm.binkley.basilisk.TestListener
import hm.binkley.basilisk.chef.ChefRecord
import hm.binkley.basilisk.db.testTransaction
import hm.binkley.basilisk.recipe.RecipeRecord
import hm.binkley.basilisk.recipe.RecipeStatus.PLANNING
import hm.binkley.basilisk.source.SourceRecord
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
        const val name = "RHUBARB"
        const val code = "ING789"
    }

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
            val chef = ChefRecord.new {
                this.name = "CHEF BOB"
                this.code = "CHEF123"
            }
            chef.flush()
            val source = SourceRecord.new {
                this.name = name
                this.code = "SRC012"
            }
            source.flush()
            IngredientRecord.new {
                this.code = code
                this.chef = chef
                this.source = source
            }.flush()

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
            val chef = ChefRecord.new {
                this.name = "CHEF BOB"
                this.code = "CHEF123"
            }
            chef.flush()
            val source = SourceRecord.new {
                this.name = name
                this.code = "SRC012"
            }
            source.flush()
            val recipe = RecipeRecord.new {
                this.name = "TASTY PIE"
                this.code = "REC456"
                this.chef = chef
                status = PLANNING
            }
            recipe.flush()
            IngredientRecord.new {
                this.code = code
                this.chef = chef
                this.source = source
                this.recipe = recipe
            }.flush()

            val ingredient = ingredients.byCode(code)!!

            expect(ingredient.code).toBe(code)
            expect(ingredient.name).toBe(name)
            expect(ingredient).isA<UsedIngredient> { }
        }
    }

    @Test
    fun shouldPublishSaveEvents() {
        val name = name
        val code = code

        testTransaction {
            val chef = ChefRecord.new {
                this.name = "CHEF BOB"
                this.code = "CHEF123"
            }
            chef.flush()
            val source = SourceRecord.new {
                this.name = name
                this.code = "SRC012"
            }
            source.flush()
            val recipe = RecipeRecord.new {
                this.name = "TASTY PIE"
                this.code = "REC456"
                this.chef = chef
                status = PLANNING
            }
            recipe.flush()
            val record = IngredientRecord.new {
                this.code = code
                this.chef = chef
                this.source = source
                this.recipe = recipe
            }
            val ingredient = UnusedIngredient(record, ingredients)

            ingredient.save()

            expect(listener.received).containsExactly(
                    IngredientSavedEvent(ingredient))
        }
    }
}
