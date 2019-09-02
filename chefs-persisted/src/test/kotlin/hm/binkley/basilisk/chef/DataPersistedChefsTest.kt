package hm.binkley.basilisk.chef

import ch.tutteli.atrium.api.cc.en_GB.containsExactly
import ch.tutteli.atrium.api.cc.en_GB.isEmpty
import ch.tutteli.atrium.api.cc.en_GB.toBe
import ch.tutteli.atrium.api.cc.en_GB.toThrow
import ch.tutteli.atrium.verbs.expect
import hm.binkley.basilisk.TestListener
import hm.binkley.basilisk.chef.Chefs.Companion.FIT
import io.micronaut.test.annotation.MicronautTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import javax.inject.Inject

@MicronautTest
@TestInstance(PER_CLASS)
internal class DataPersistedChefsTest {
    companion object {
        const val code = "CHEF123"
        const val name = "CHEF BOB"
    }

    @Inject
    lateinit var chefs: DataPersistedChefs
    @Inject
    lateinit var listener: TestListener<ChefSavedEvent>

    @BeforeEach
    fun setUp() {
        listener.reset()
    }

    @Test
    fun shouldFindNoChef() {
        expect(chefs.all()).isEmpty()
    }

    @Test
    fun shouldRoundTrip() {
        val unsaved = ChefResource(code, name)
        val saved = chefs.new(unsaved)

        expect(ChefResource(saved)).toBe(unsaved)

        val found = chefs.byCode(code)!!

        expect(found).toBe(saved)
    }

    @Test
    fun shouldPublishSaveEvents() {
        val firstSnapshot = ChefResource(code, name, FIT)
        val secondSnapshot = ChefResource(code, "CHEF ROBERT", "OUT")

        val chef = chefs.new(firstSnapshot)

        listener.expectNext.containsExactly(ChefSavedEvent(
                null, chef))

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

        listener.expectNext.containsExactly(ChefSavedEvent(
                firstSnapshot, chef))

        chef.update {
            delete()
        }

        listener.expectNext.containsExactly(ChefSavedEvent(
                secondSnapshot, null))
    }

    @Test
    fun shouldSkipPublishSaveEventsIfUnchanged() {
        val snapshot = ChefResource(code, name, FIT)

        val chef = chefs.new(snapshot)

        listener.expectNext.containsExactly(ChefSavedEvent(
                null, chef))

        chef.update {
            save()
        }

        listener.expectNext.isEmpty()
    }

    @Test
    fun shouldComplainIfUpdatingAfterDeleted() {
        val snapshot = ChefResource(code, name, FIT)
        val chef = chefs.new(snapshot)

        chef.update {
            delete()
        }

        expect {
            chef.update {}
        }.toThrow<java.lang.IllegalStateException> {}
    }
}
