package hm.binkley.basilisk.chef

import ch.tutteli.atrium.api.cc.en_GB.containsExactly
import ch.tutteli.atrium.api.cc.en_GB.isEmpty
import ch.tutteli.atrium.api.cc.en_GB.toBe
import ch.tutteli.atrium.verbs.expect
import hm.binkley.basilisk.TestListener
import hm.binkley.basilisk.chef.Chefs.Companion.FIT
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
            val ingredient = chefs.byCode(code)

            expect(ingredient).toBe(null)
        }
    }

    @Test
    fun shouldRoundTrip() {
        testTransaction {
            chefs.new(name, code)
            val chef = chefs.byCode(code)!!

            expect(chef.code).toBe(code)
            expect(chef.name).toBe(name)
        }
    }

    @Test
    fun shouldPublishSaveEvents() {
        testTransaction {
            val firstSnapshot = ChefResource(name, code, FIT)
            val secondSnapshot = ChefResource("CHEF ROBERT", code, "OUT")

            val chef = chefs.new(
                    firstSnapshot.name, firstSnapshot.code,
                    firstSnapshot.health)

            listener.expectNext.containsExactly(ChefSavedEvent(
                    null, chef))

            chef.update {
                this.name = secondSnapshot.name
                this.health = secondSnapshot.health
                save()
            }

            listener.expectNext.containsExactly(ChefSavedEvent(
                    firstSnapshot, chef))

            chef.update {
                delete()
            }

            listener.expectNext.containsExactly(ChefSavedEvent(
                    secondSnapshot, null))
        }
    }

    @Test
    fun shouldSkipPublishSaveEventsIfUnchanged() {
        testTransaction {
            val snapshot = ChefResource(name, code, FIT)

            val chef = chefs.new(
                    snapshot.name, snapshot.code,
                    snapshot.health)

            listener.expectNext.containsExactly(ChefSavedEvent(
                    null, chef))

            chef.update {
                save()
            }

            listener.expectNext.isEmpty()
        }
    }
}
