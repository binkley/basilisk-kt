package hm.binkley.basilisk

import ch.tutteli.atrium.api.cc.en_GB.*
import ch.tutteli.atrium.verbs.expect
import io.micronaut.test.annotation.MicronautTest
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
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
            chef.flush()
            val recipe = RecipeRecord.new {
                name = "TASTY STEW"
                this.chef = chef
            }
            recipe.flush()
            val sourceA = SourceRecord.new {
                name = "RHUBARB"
            }
            sourceA.flush()
            val ingredientA = IngredientRecord.new {
                source = sourceA
                this.chef = chef
                this.recipe = recipe
            }
            ingredientA.flush()
            val sourceB = SourceRecord.new {
                name = "NUTMEG"
            }
            sourceB.flush()
            val ingredientB = IngredientRecord.new {
                source = sourceB
                this.chef = chef
                this.recipe = recipe
            }
            ingredientB.flush()

            val chefs = ChefRecord.all()
            expect(chefs).contains.inAnyOrder.only.values(chef)

            val recipes = RecipeRecord.all()
            expect(recipes).contains.inAnyOrder.only.values(recipe)

            val sources = SourceRecord.all()
            expect(sources).contains.inAnyOrder.only.values(sourceA, sourceB)

            val ingredients = IngredientRecord.all()
            expect(ingredients).contains.inAnyOrder.only.values(ingredientA, ingredientB)

            val readBack = RecipeRecord[recipe.id]
            expect(readBack.ingredients).contains.inAnyOrder.only.values(ingredientA, ingredientB)

            rollback() // TODO: Integrate with @MicronautTest rollbacks
        }
    }

    @Test
    fun shouldRoundTripMultiple() {
        transaction {
            val chef = ChefRecord.new {
                name = "CHEF BOB"
            }
            chef.flush()
            val trip = TripRecord.new {
                name = "ROUND THE WORLD"
                this.chef = chef
            }
            trip.flush()
            val locationA = LocationRecord.new {
                name = "DALLAS"
            }
            locationA.flush()
            val locationB = LocationRecord.new {
                name = "MELBOURNE"
            }
            locationB.flush()
            val locationC = LocationRecord.new {
                name = "TOKYO"
            }
            locationB.flush()
            val tripStartAt = DateTime.parse("2011-02-03T14:15:16Z")
            val legA = LegRecord.new {
                this.trip = trip
                start = locationA
                startAt = tripStartAt
                end = locationB
                endAt = tripStartAt.plusDays(1)
            }
            legA.flush()
            val legB = LegRecord.new {
                this.trip = trip
                start = locationB
                startAt = tripStartAt.plusDays(2)
                end = locationC
                endAt = tripStartAt.plusDays(3)
            }
            legB.flush()

            expect(trip.legs).contains.inAnyOrder.only.values(legA, legB)

            rollback()
        }
    }
}
