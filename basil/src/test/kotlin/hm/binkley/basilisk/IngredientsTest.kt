package hm.binkley.basilisk

import ch.tutteli.atrium.api.cc.en_GB.containsExactly
import ch.tutteli.atrium.api.cc.en_GB.isA
import ch.tutteli.atrium.api.cc.en_GB.toBe
import ch.tutteli.atrium.verbs.expect
import io.micronaut.context.event.ApplicationEventListener
import io.micronaut.context.event.ApplicationEventPublisher
import io.micronaut.test.annotation.MicronautTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import javax.inject.Inject
import javax.inject.Singleton

@Singleton // TODO: How to make this 'object', not 'class'?
class TestListener : ApplicationEventListener<IngredientSavedEvent> {
    private val _received = mutableListOf<IngredientSavedEvent>()
    val received
        get() = _received

    override fun onApplicationEvent(event: IngredientSavedEvent) {
        _received.add(event)
    }
}

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
    lateinit var chefs: Chefs
    @Inject
    lateinit var publisher: ApplicationEventPublisher
    @Inject
    lateinit var listener: TestListener

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
            }
            recipe.flush()
            val record = IngredientRecord.new {
                this.code = code
                this.chef = chef
                this.source = source
                this.recipe = recipe
            }
            val ingredient = UnusedIngredient(
                    record, chefs, publisher)

            ingredient.save()

            expect(listener.received).containsExactly(
                    IngredientSavedEvent(ingredient))
        }
    }
}
