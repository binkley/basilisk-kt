package hm.binkley.basilisk

import ch.tutteli.atrium.api.cc.en_GB.containsExactly
import ch.tutteli.atrium.verbs.expect
import io.micronaut.test.annotation.MicronautTest
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.testcontainers.junit.jupiter.Testcontainers

@MicronautTest
@TestInstance(PER_CLASS)
@Testcontainers
class PersistenceSpec {
    @Test
    fun shouldRoundTripSimple() {
        transaction {
            val chef = ChefRecord.new {
                name = "CHEF BOB"
            }
            chef.flush()
            val chefs = ChefRecord.all()
            expect(chefs).containsExactly(chef)

            rollback() // TODO: Integrate with @MicronautTest rollbacks
        }
    }

    @Test
    fun shouldRoundTripComplex() {
        transaction {
            val chef = ChefRecord.new {
                name = "CHEF BOB"
            }
            val recipe = RecipeRecord.new {
                name = "TASTY STEW"
                this.chef = chef
            }
            recipe.flush()
            val chefs = ChefRecord.all()
            expect(chefs).containsExactly(chef)
            val recipes = RecipeRecord.all()
            expect(recipes).containsExactly(recipe)

            rollback() // TODO: Integrate with @MicronautTest rollbacks
        }
    }
}
