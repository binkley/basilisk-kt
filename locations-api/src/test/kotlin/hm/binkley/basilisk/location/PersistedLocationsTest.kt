package hm.binkley.basilisk.location

import ch.tutteli.atrium.api.cc.en_GB.containsExactly
import ch.tutteli.atrium.api.cc.en_GB.isEmpty
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
internal class PersistedLocationsTest {
    companion object {
        const val name = "LOCATION BOB"
        const val code = "LOCATION123"
    }

    @Inject
    lateinit var locations: PersistedLocations
    @Inject
    lateinit var listener: TestListener<LocationSavedEvent>

    @AfterEach
    fun tearDown() {
        listener.reset()
    }

    @Test
    fun shouldFindNoLocation() {
        testTransaction {
            val ingredient = locations.byCode(code)

            expect(ingredient).toBe(null)
        }
    }

    @Test
    fun shouldRoundTrip() {
        testTransaction {
            val firstSnapshot = LocationResource(code, name)
            locations.new(firstSnapshot)
            val location = locations.byCode(code)!!

            expect(location.code).toBe(code)
            expect(location.name).toBe(name)
        }
    }

    @Test
    fun shouldPublishSaveEvents() {
        testTransaction {
            val firstSnapshot = LocationResource(code, name)
            val secondSnapshot = LocationResource(code, "LOCATION ROBERT")

            val location = locations.new(firstSnapshot)

            listener.expectNext.containsExactly(LocationSavedEvent(
                    null, LocationResource(location)))

            // Saving without changing does not publish an update
            location.update {
                save()
            }

            listener.expectNext.isEmpty()

            // Update twice, check first snapshot is original values
            location.update {
                this.name = firstSnapshot.name + "X"
            }

            location.update {
                this.name = secondSnapshot.name
                save()
            }

            listener.expectNext.containsExactly(LocationSavedEvent(
                    firstSnapshot, LocationResource(location)))

            location.update {
                delete()
            }

            listener.expectNext.containsExactly(LocationSavedEvent(
                    secondSnapshot, null))
        }
    }

    @Test
    fun shouldSkipPublishSaveEventsIfUnchanged() {
        testTransaction {
            val snapshot = LocationResource(code, name)

            val location = locations.new(snapshot)

            listener.expectNext.containsExactly(LocationSavedEvent(
                    null, LocationResource(location)))

            location.update {
                save()
            }

            listener.expectNext.isEmpty()
        }
    }
}
