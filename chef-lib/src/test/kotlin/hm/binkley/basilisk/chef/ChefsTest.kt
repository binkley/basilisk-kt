package hm.binkley.basilisk.chef

import ch.tutteli.atrium.api.cc.en_GB.toBe
import ch.tutteli.atrium.verbs.expect
import hm.binkley.basilisk.testTransaction
import io.micronaut.test.annotation.MicronautTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import javax.inject.Inject

@MicronautTest
@TestInstance(PER_CLASS)
internal class ChefsTest {
    companion object {
        const val name = "CHEF BOB"
        const val code = "CHEF123"
    }

    @Inject
    lateinit var chefs: Chefs

    @Test
    fun shouldFindNoChef() {
        testTransaction {
            val ingredient = chefs.chef(
                    code)

            expect(ingredient).toBe(null)
        }
    }

    @Test
    fun shouldRoundTrip() {
        testTransaction {
            chefs.create(name,
                    code)
            val chef = chefs.chef(
                    code)

            expect(chef!!.code).toBe(
                    code)
            expect(chef.name).toBe(
                    name)
        }
    }
}
