package hm.binkley.basilisk.location

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
internal class LocationsTest {
    companion object {
        const val name = "LOCATION BOB"
        const val code = "LOCATION123"
    }

    @Inject
    lateinit var locations: Locations
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
            locations.new(name, code)
            val location = locations.byCode(code)!!

            expect(location.code).toBe(code)
            expect(location.name).toBe(name)
        }
    }

    @Test
    fun shouldPublishSaveEvents() {
        testTransaction {
            val firstSnapshot = LocationResource(name, code)
            val secondSnapshot = LocationResource("LOCATION ROBERT", code)

            val location = locations.new(
                    firstSnapshot.name, firstSnapshot.code)

            listener.expect.containsExactly(LocationSavedEvent(
                    null, location))

            location.update {
                this.name = secondSnapshot.name
                save()
            }

            listener.expect.containsExactly(LocationSavedEvent(
                    firstSnapshot, location))

            location.update {
                delete()
            }

            listener.expect.containsExactly(LocationSavedEvent(
                    secondSnapshot, null))
        }
    }
}
