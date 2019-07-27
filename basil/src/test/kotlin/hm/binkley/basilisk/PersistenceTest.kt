package hm.binkley.basilisk

import ch.tutteli.atrium.api.cc.en_GB.contains
import ch.tutteli.atrium.api.cc.en_GB.containsExactly
import ch.tutteli.atrium.api.cc.en_GB.toBe
import ch.tutteli.atrium.verbs.expect
import hm.binkley.basilisk.chef.ChefRecord
import hm.binkley.basilisk.ingredient.IngredientRecord
import hm.binkley.basilisk.leg.LegRecord
import hm.binkley.basilisk.location.LocationRecord
import hm.binkley.basilisk.recipe.RecipeRecord
import hm.binkley.basilisk.source.SourceRecord
import hm.binkley.basilisk.trip.TripRecord
import io.micronaut.test.annotation.MicronautTest
import org.jetbrains.exposed.sql.SizedCollection
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS

/**
 * @todo Why all the nestiness?
 * @todo Is there a nicer way to rollback each test than `testTransaction`?
 */
@MicronautTest
@TestInstance(PER_CLASS)
internal class PersistenceTest {
    @Test
    fun shouldRoundTripSimple() {
        testTransaction {
            val chef = ChefRecord.new {
                name = "CHEF BOB"
                code = "CHEF123"
            }
            chef.flush()
            val chefs = ChefRecord.all()
            expect(chefs).containsExactly(chef)
        }
    }

    @Test
    fun shouldRoundTripComplex() {
        testTransaction {
            val chef = ChefRecord.new {
                name = "CHEF BOB"
                code = "CHEF123"
            }
            chef.flush()
            val recipe = RecipeRecord.new {
                name = "TASTY STEW"
                code = "REC456"
                this.chef = chef
            }
            recipe.flush()
            val sourceA = SourceRecord.new {
                name = "RHUBARB"
                code = "SRC0123"
            }
            sourceA.flush()
            val ingredientA = IngredientRecord.new {
                source = sourceA
                code = "ING789"
                this.chef = chef
                this.recipe = recipe
            }
            ingredientA.flush()
            val sourceB = SourceRecord.new {
                name = "NUTMEG"
                code = "SRC345"
            }
            sourceB.flush()
            val ingredientB = IngredientRecord.new {
                source = sourceB
                code = "ING890"
                this.chef = chef
                this.recipe = recipe
            }
            ingredientB.flush()

            val chefs = ChefRecord.all()
            expect(chefs).contains(chef)

            val recipes = RecipeRecord.all()
            expect(recipes).contains(recipe)

            val sources = SourceRecord.all()
            expect(sources).contains(sourceA, sourceB)

            val ingredients = IngredientRecord.all()
            expect(ingredients).contains(ingredientA, ingredientB)

            val readBack = RecipeRecord[recipe.id]
            expect(readBack.ingredients).contains(ingredientA, ingredientB)
        }
    }

    @Test
    fun shouldRoundTripMultiple() {
        testTransaction {
            val chef = ChefRecord.new {
                name = "CHEF BOB"
                code = "CHEF123"
            }
            chef.flush()
            val trip = TripRecord.new {
                name = "ROUND THE WORLD"
                this.chef = chef
            }
            trip.flush()
            val locationA = LocationRecord.new {
                name = "DALLAS"
                code = "DAL"
            }
            locationA.flush()
            val locationB = LocationRecord.new {
                name = "MELBOURNE"
                code = "MEL"
            }
            locationB.flush()
            val locationC = LocationRecord.new {
                name = "TOKYO"
                code = "TOK"
            }
            locationC.flush()
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

            expect(trip.legs).contains(legA, legB)
        }
    }

    @Test
    fun shouldHaveLocationsPerSource() {
        testTransaction {
            val locations = transaction {
                val locationA = LocationRecord.new {
                    name = "DALLAS"
                    code = "DAL"
                }
                val locationB = LocationRecord.new {
                    name = "MELBOURNE"
                    code = "MEL"
                }
                val locationC = LocationRecord.new {
                    name = "TOKYO"
                    code = "TOK"
                }

                listOf(locationA, locationB, locationC)
            }

            val source = transaction {
                val source = SourceRecord.new {
                    name = "RHUBARB"
                    code = "SRC012"
                }

                source
            }

            transaction {
                source.locations = SizedCollection(locations)
            }

            expect(SourceRecord.findById(source.id)!!.locations.toSet())
                    .toBe(locations.toSet())
        }
    }

    @Test
    fun shouldHaveLocationsPerIngredient() {
        testTransaction {
            val locations = transaction {
                val locationA = LocationRecord.new {
                    name = "DALLAS"
                    code = "DAL"
                }
                val locationB = LocationRecord.new {
                    name = "MELBOURNE"
                    code = "MEL"
                }
                val locationC = LocationRecord.new {
                    name = "TOKYO"
                    code = "TOK"
                }

                listOf(locationA, locationB, locationC)
            }

            val ingredient = transaction {
                val chef = ChefRecord.new {
                    name = "CHEF BOB"
                    code = "CHEF123"
                }
                val source = SourceRecord.new {
                    name = "RHUBARB"
                    code = "SRC012"
                }
                val ingredient = IngredientRecord.new {
                    code = "ING789"
                    this.chef = chef
                    this.source = source
                }

                ingredient
            }

            transaction {
                ingredient.locations = SizedCollection(locations)
            }

            expect(IngredientRecord.findById(
                    ingredient.id)!!.locations.toSet())
                    .toBe(locations.toSet())
        }
    }

    @Test
    fun shouldHaveLocationsPerRecipe() {
        testTransaction {
            val locations = transaction {
                val locationA = LocationRecord.new {
                    name = "DALLAS"
                    code = "DAL"
                }
                val locationB = LocationRecord.new {
                    name = "MELBOURNE"
                    code = "MEL"
                }
                val locationC = LocationRecord.new {
                    name = "TOKYO"
                    code = "TOK"
                }

                listOf(locationA, locationB, locationC)
            }

            val recipe = transaction {
                val chef = ChefRecord.new {
                    name = "CHEF BOB"
                    code = "CHEF123"
                }
                val recipe = RecipeRecord.new {
                    name = "TASTY PIE"
                    code = "REC456"
                    this.chef = chef
                }

                recipe
            }

            transaction {
                recipe.locations = SizedCollection(locations)
            }

            expect(RecipeRecord.findById(recipe.id)!!.locations.toSet())
                    .toBe(locations.toSet())
        }
    }
}
