package hm.binkley.basilisk

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
internal class ChefsTest {
    @Inject
    lateinit var chefs: Chefs

    @Test
    fun shouldFindNoChef() {
        val code = "CHEF123"
        testTransaction {
            val ingredient = chefs.chef(code)

            expect(ingredient).toBe(null)
        }
    }

    @Test
    fun shouldFindChef() {
        val name = "CHEF BOB"
        val code = "CHEF123"
        testTransaction {
            ChefRecord.new {
                this.name = name
                this.code = code
            }.flush()

            val chef = chefs.chef(code)

            expect(chef!!.code).toBe(code)
            expect(chef.name).toBe(name)
        }
    }
}
