package hm.binkley.basilisk.location

import ch.tutteli.atrium.api.cc.en_GB.containsExactly
import ch.tutteli.atrium.api.cc.en_GB.isEmpty
import ch.tutteli.atrium.api.cc.en_GB.toBe
import ch.tutteli.atrium.verbs.expect
import hm.binkley.basilisk.TestListener
import hm.binkley.basilisk.db.testTransaction
import io.micronaut.test.annotation.MicronautTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import javax.inject.Inject

@MicronautTest
@TestInstance(PER_CLASS)
internal class PersistedLocationsTest {
    companion object {
        const val code = "DAL"
        const val name = "The Dallas Yellow Rose"
    }

    @Inject
    lateinit var locations: PersistedLocations
    @Inject
    lateinit var listener: TestListener<LocationChangedEvent>

    @BeforeEach
    fun setUp() {
        listener.reset()
    }

    @Test
    fun shouldFindNoLocation() {
        testTransaction {
            val location = locations.byCode(code)

            expect(location).toBe(null)
        }
    }

    @Test
    fun shouldRoundTrip() {
        testTransaction {
            locations.new(LocationResource(code, name))
            val location = locations.byCode(code)!!

            expect(location.code).toBe(code)
            expect(location.name).toBe(name)
        }
    }

    @Test
    fun shouldPublishSaveEvents() {
        testTransaction {
            val firstSnapshot = LocationResource(code, name)
            val secondSnapshot =
                    LocationResource(code, "LOCATION ROBERT")

            val location = locations.new(firstSnapshot)

            // No event until saved
            listener.expectNext.isEmpty()

            location.update {
                save()
            }

            listener.expectNext.containsExactly(
                    LocationChangedEvent(null, LocationResource(location)))

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

            listener.expectNext.containsExactly(LocationChangedEvent(
                    firstSnapshot, LocationResource(location)))

            location.update {
                delete()
            }

            listener.expectNext.containsExactly(LocationChangedEvent(
                    secondSnapshot, null))
        }
    }
}
