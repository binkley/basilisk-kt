package hm.binkley.basilisk

import ch.tutteli.atrium.api.cc.en_GB.containsExactly
import ch.tutteli.atrium.api.cc.en_GB.toBe
import ch.tutteli.atrium.verbs.expect
import hm.binkley.basilisk.ingredient.UsedIngredient
import hm.binkley.basilisk.location.PersistedLocation
import hm.binkley.basilisk.recipe.Recipe
import hm.binkley.basilisk.source.Source
import io.mockk.every
import io.mockk.mockk
import org.jetbrains.exposed.sql.SizedCollection
import org.jetbrains.exposed.sql.emptySized
import org.junit.jupiter.api.Test

class RestrictionsTest {
    @Test
    fun `should have no restrictions`() {
        val source = mockk<Source>()
        every { source.locations } answers { emptySized() }
        val ingredient = mockk<UsedIngredient>()
        every { ingredient.source } answers { source }
        every { ingredient.locations } answers { emptySized() }
        val recipe = mockk<Recipe>()
        every { recipe.ingredients } answers { SizedCollection(ingredient) }
        every { recipe.locations } answers { emptySized() }

        expect(recipe.restrictions().empty()).toBe(true)
    }

    @Test
    fun `should use restrictions from sources`() {
        val source = mockk<Source>()
        val location = mockk<PersistedLocation>()
        every { source.locations } answers { SizedCollection(location) }
        val ingredient = mockk<UsedIngredient>()
        every { ingredient.source } answers { source }
        every { ingredient.locations } answers { emptySized() }
        val recipe = mockk<Recipe>()
        every { recipe.ingredients } answers { SizedCollection(ingredient) }
        every { recipe.locations } answers { emptySized() }

        expect(recipe.restrictions()).containsExactly(location)
    }

    @Test
    fun `should use restrictions from ingredients`() {
        val source = mockk<Source>()
        val locationA = mockk<PersistedLocation>()
        every { source.locations } answers { SizedCollection(locationA) }
        val ingredient = mockk<UsedIngredient>()
        every { ingredient.source } answers { source }
        val locationB = mockk<PersistedLocation>()
        every { ingredient.locations } answers { SizedCollection(locationB) }
        val recipe = mockk<Recipe>()
        every { recipe.ingredients } answers { SizedCollection(ingredient) }
        every { recipe.locations } answers { emptySized() }

        expect(recipe.restrictions()).containsExactly(locationB)
    }

    @Test
    fun `should use restrictions from recipes`() {
        val source = mockk<Source>()
        every { source.locations } answers { emptySized() }
        val ingredient = mockk<UsedIngredient>()
        every { ingredient.source } answers { source }
        val locationA = mockk<PersistedLocation>()
        every { ingredient.locations } answers { SizedCollection(locationA) }
        val recipe = mockk<Recipe>()
        every { recipe.ingredients } answers { SizedCollection(ingredient) }
        val locationB = mockk<PersistedLocation>()
        every { recipe.locations } answers { SizedCollection(locationB) }

        expect(recipe.restrictions()).containsExactly(locationB)
    }

    @Test
    fun `should intersect ingredient locations`() {
        val source = mockk<Source>()
        every { source.locations } answers { emptySized() }
        val ingredientA = mockk<UsedIngredient>()
        every { ingredientA.source } answers { source }
        val locationA = mockk<PersistedLocation>()
        val locationC = mockk<PersistedLocation>()
        every { ingredientA.locations } answers {
            SizedCollection(locationA, locationC)
        }
        val ingredientB = mockk<UsedIngredient>()
        every { ingredientB.source } answers { source }
        val locationB = mockk<PersistedLocation>()
        every { ingredientB.locations } answers {
            SizedCollection(locationB, locationC)
        }
        val recipe = mockk<Recipe>()
        every { recipe.ingredients } answers {
            SizedCollection(ingredientA, ingredientB)
        }
        every { recipe.locations } answers { emptySized() }

        expect(recipe.restrictions()).containsExactly(locationC)
    }
}
