package hm.binkley.basilisk

import ch.tutteli.atrium.api.cc.en_GB.isA
import ch.tutteli.atrium.api.cc.en_GB.toBe
import ch.tutteli.atrium.verbs.expect
import io.micronaut.test.annotation.MicronautTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.testcontainers.junit.jupiter.Testcontainers
import javax.inject.Inject

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Testcontainers
internal class IngredientsTest {
    companion object {
        val name = "RHUBARB"
        val code = "ING789"
    }

    @Inject
    lateinit var ingredients: Ingredients

    @Test
    fun shouldFindNoIngredient() {
        testTransaction {
            val ingredient = ingredients.ingredient(code)

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

            val ingredient = ingredients.ingredient(code)

            expect(ingredient!!.code).toBe(code)
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
            }
            recipe.flush()
            IngredientRecord.new {
                this.code = code
                this.chef = chef
                this.source = source
                this.recipe = recipe
            }.flush()

            val ingredient = ingredients.ingredient(code)

            expect(ingredient!!.code).toBe(code)
            expect(ingredient.name).toBe(name)
            expect(ingredient).isA<UsedIngredient> { }
        }
    }
}
