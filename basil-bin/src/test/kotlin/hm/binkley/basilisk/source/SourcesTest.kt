package hm.binkley.basilisk.source

import ch.tutteli.atrium.api.cc.en_GB.containsExactly
import ch.tutteli.atrium.api.cc.en_GB.toBe
import ch.tutteli.atrium.verbs.expect
import hm.binkley.basilisk.TestListener
import hm.binkley.basilisk.db.testTransaction
import hm.binkley.basilisk.location.LocationResource
import hm.binkley.basilisk.location.Locations
import io.micronaut.test.annotation.MicronautTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import javax.inject.Inject

@MicronautTest
@TestInstance(PER_CLASS)
internal class SourcesTest {
    companion object {
        const val name = "RHUBARB"
        const val code = "SRC012"
    }

    @Inject
    lateinit var sources: Sources
    @Inject
    lateinit var locations: Locations
    @Inject
    lateinit var listener: TestListener<SourceSavedEvent>

    @AfterEach
    fun tearDown() {
        listener.reset()
    }

    @Test
    fun shouldFindNoSource() {
        testTransaction {
            val ingredient = sources.byCode(code)

            expect(ingredient).toBe(null)
        }
    }

    @Test
    fun shouldRoundTrip() {
        testTransaction {
            sources.new(name, code)
            val source = sources.byCode(code)!!

            expect(source.code).toBe(code)
            expect(source.name).toBe(name)
        }
    }

    @Test
    fun shouldPublishSaveEvents() {
        val locationName = "The Dallas Yellow Rose"
        val locationCode = "DAL"

        testTransaction {
            val location = locations.new(locationName, locationCode)
            listener.reset()

            val firstSnapshot = SourceResource(
                    name, code, listOf(LocationResource(location)))
            val secondSnapshot = SourceResource(
                    "LIME", code, listOf())
            val thirdSnapshot = SourceResource(
                    "LIME", code, listOf(LocationResource(location)))

            val source = sources.new(
                    firstSnapshot.name, firstSnapshot.code,
                    mutableListOf(location))

            expect(listener.received).containsExactly(SourceSavedEvent(
                    null, source))
            listener.reset()

            source.update {
                this.name = secondSnapshot.name
                this.locations.clear()
                save()
            }

            expect(listener.received).containsExactly(SourceSavedEvent(
                    firstSnapshot, source))
            listener.reset()

            source.update {
                this.locations = mutableListOf(location) // Change our minds
                save()
            }

            expect(listener.received).containsExactly(SourceSavedEvent(
                    secondSnapshot, source))
            listener.reset()

            source.update {
                delete()
            }

            expect(listener.received).containsExactly(SourceSavedEvent(
                    thirdSnapshot, null))
        }
    }
}
