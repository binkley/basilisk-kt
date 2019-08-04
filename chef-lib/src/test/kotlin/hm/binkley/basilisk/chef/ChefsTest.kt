package hm.binkley.basilisk.chef

import ch.tutteli.atrium.api.cc.en_GB.containsExactly
import ch.tutteli.atrium.api.cc.en_GB.toBe
import ch.tutteli.atrium.verbs.expect
import hm.binkley.basilisk.TestListener
import hm.binkley.basilisk.db.testTransaction
import io.micronaut.test.annotation.MicronautTest
import org.junit.jupiter.api.AfterEach
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
    @Inject
    lateinit var listener: TestListener<ChefSavedEvent>

    @AfterEach
    fun tearDown() {
        listener.reset()
    }

    @Test
    fun shouldFindNoChef() {
        testTransaction {
            val ingredient = chefs.chef(code)

            expect(ingredient).toBe(null)
        }
    }

    @Test
    fun shouldRoundTrip() {
        testTransaction {
            chefs.new(name, code)
            val chef = chefs.chef(code)!!

            expect(chef.code).toBe(code)
            expect(chef.name).toBe(name)
        }
    }

    @Test
    fun shouldPublishSaveEvents() {
        val name = name
        val code = code

        testTransaction {
            val firstSnapshot = ChefResource(name, code)
            val secondSnapshot = ChefResource("CHEF ROBERT", code)

            val chef = chefs.new(firstSnapshot.name, firstSnapshot.code)

            expect(listener.received).containsExactly(ChefSavedEvent(
                    null, chef.mutable(null)))
            listener.reset()

            chef.mutable().apply {
                this.name = secondSnapshot.name
            }.save()

            expect(listener.received).containsExactly(ChefSavedEvent(
                    firstSnapshot, chef.mutable(firstSnapshot)))
            listener.reset()

            chef.mutable().delete()

            expect(listener.received).containsExactly(ChefSavedEvent(
                    secondSnapshot, null))
        }
    }
}
