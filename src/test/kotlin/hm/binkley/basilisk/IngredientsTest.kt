package hm.binkley.basilisk

import ch.tutteli.atrium.api.cc.en_GB.toBe
import ch.tutteli.atrium.verbs.expect
import io.micronaut.context.event.ApplicationEventPublisher
import io.micronaut.test.annotation.MicronautTest
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.testcontainers.junit.jupiter.Testcontainers
import javax.inject.Inject

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Testcontainers
internal class IngredientsTest {
    @Inject
    lateinit var publisher: ApplicationEventPublisher

    @Test
    fun shouldFindUnusedIngredient() {
        val code = "ING789"
        val name = "RHUBARB"
        transaction {
            val chef = ChefRecord.new {
                this.name = "CHEF BOB"
                this.code = "CHEF123"
            }
            chef.flush()
            val source = SourceRecord.new {
                this.name = name
            }
            source.flush()
            val record = IngredientRecord.new {
                this.code = code
                this.chef = chef
                this.source = source
            }
            record.flush()

            val ingredient = Ingredients(publisher).ingredient(code)

            expect(ingredient.code).toBe(code)
            expect(ingredient.name).toBe(name)
        }
    }
}
