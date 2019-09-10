package hm.binkley.basilisk.chef

import ch.tutteli.atrium.api.cc.en_GB.containsExactly
import ch.tutteli.atrium.api.cc.en_GB.isEmpty
import ch.tutteli.atrium.api.cc.en_GB.toBe
import ch.tutteli.atrium.verbs.expect
import hm.binkley.basilisk.TestListener
import hm.binkley.basilisk.chef.Chefs.Companion.FIT
import hm.binkley.basilisk.db.testTransaction
import io.micronaut.test.annotation.MicronautTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import javax.inject.Inject

@MicronautTest
@TestInstance(PER_CLASS)
internal class PersistedChefsTest {
    companion object {
        const val code = "CHEF123"
        const val name = "CHEF BOB"
    }

    @Inject
    lateinit var chefs: PersistedChefs
    @Inject
    lateinit var listener: TestListener<ChefChangedEvent>

    @BeforeEach
    fun setUp() {
        listener.reset()
    }

    @Test
    fun shouldFindNoChef() {
        testTransaction {
            val chef = chefs.byCode(code)

            expect(chef).toBe(null)
        }
    }

    @Test
    fun shouldRoundTrip() {
        testTransaction {
            chefs.new(ChefResource(code, name))
            val chef = chefs.byCode(code)!!

            expect(chef.code).toBe(code)
            expect(chef.name).toBe(name)
        }
    }

    @Test
    fun shouldPublishSaveEvents() {
        testTransaction {
            val firstSnapshot = ChefResource(code, name, FIT)
            val secondSnapshot = ChefResource(code, "CHEF ROBERT", "OUT")

            val chef = chefs.new(firstSnapshot)

            // No event until saved
            listener.expectNext.isEmpty()

            chef.update {
                save()
            }

            listener.expectNext.containsExactly(
                    ChefChangedEvent(null, ChefResource(chef)))

            // Saving without changing does not publish an update
            chef.update {
                save()
            }

            listener.expectNext.isEmpty()

            // Update twice, check first snapshot is original values
            chef.update {
                this.name = firstSnapshot.name + "X"
            }

            chef.update {
                this.name = secondSnapshot.name
                this.health = secondSnapshot.health
                save()
            }

            listener.expectNext.containsExactly(ChefChangedEvent(
                    firstSnapshot, ChefResource(chef)))

            chef.update {
                delete()
            }

            listener.expectNext.containsExactly(ChefChangedEvent(
                    secondSnapshot, null))
        }
    }
}
