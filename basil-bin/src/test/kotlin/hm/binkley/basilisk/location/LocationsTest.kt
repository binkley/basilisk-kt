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
        const val name = "The Dallas Yellow Rose"
        const val code = "DAL"
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
            val ingredient = locations.location(code)

            expect(ingredient).toBe(null)
        }
    }

    @Test
    fun shouldRoundTrip() {
        testTransaction {
            locations.new(name, code)
            val location = locations.location(code)!!

            expect(location.code).toBe(code)
            expect(location.name).toBe(name)
        }
    }

    @Test
    fun shouldPublishSaveEvents() {
        val name = name
        val code = code

        testTransaction {
            val firstSnapshot = LocationResource(name, code)
            val secondSnapshot = LocationResource("New Location", code)

            val location = locations.new(
                    firstSnapshot.name, firstSnapshot.code)

            expect(listener.received).containsExactly(LocationSavedEvent(
                    null, location.mutable(null)))
            listener.reset()

            location.mutable().apply {
                this.name = secondSnapshot.name
            }.save()

            expect(listener.received).containsExactly(LocationSavedEvent(
                    firstSnapshot, location.mutable(firstSnapshot)))
            listener.reset()

            location.mutable().delete()

            expect(listener.received).containsExactly(LocationSavedEvent(
                    secondSnapshot, null))
        }
    }
}