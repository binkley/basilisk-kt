package hm.binkley.basilisk.source

import ch.tutteli.atrium.api.cc.en_GB.containsExactly
import ch.tutteli.atrium.api.cc.en_GB.isEmpty
import ch.tutteli.atrium.api.cc.en_GB.toBe
import ch.tutteli.atrium.verbs.expect
import hm.binkley.basilisk.TestListener
import hm.binkley.basilisk.db.testTransaction
import hm.binkley.basilisk.location.LocationResource
import hm.binkley.basilisk.location.PersistedLocations
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
    lateinit var locations: PersistedLocations
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
        val locationAName = "The Dallas Yellow Rose"
        val locationACode = "DAL"
        val locationBName = "Melbourne Pink Heath"
        val locationBCode = "MEL"

        testTransaction {
            val locationA = locations.new(
                    LocationResource(locationAName, locationACode))
            val locationB = locations.new(
                    LocationResource(locationBName, locationBCode))
            listener.reset()

            val firstSnapshot = SourceResource(name, code, listOf(
                    LocationResource(locationA),
                    LocationResource(locationB)))
            val secondSnapshot = SourceResource("LIME", code, listOf(
                    LocationResource(locationA)))
            val thirdSnapshot = SourceResource("LIME", code, listOf())

            val source = sources.new(
                    firstSnapshot.name, firstSnapshot.code,
                    mutableListOf(locationA, locationB))

            listener.expectNext.containsExactly(SourceSavedEvent(
                    null, source))

            source.update {
                this.name = secondSnapshot.name
                this.locations.remove(locationB)
                save()
            }

            listener.expectNext.containsExactly(SourceSavedEvent(
                    firstSnapshot, source))

            source.update {
                this.locations = mutableListOf()
                save()
            }

            listener.expectNext.containsExactly(SourceSavedEvent(
                    secondSnapshot, source))

            source.update {
                delete()
            }

            listener.expectNext.containsExactly(SourceSavedEvent(
                    thirdSnapshot, null))
        }
    }

    @Test
    fun shouldSkipPublishSaveEventsIfUnchangd() {
        val locationName = "The Dallas Yellow Rose"
        val locationCode = "DAL"

        testTransaction {
            val location = locations.new(
                    LocationResource(locationName, locationCode))
            listener.reset()

            val snapshot = SourceResource(
                    name, code, listOf(LocationResource(location)))

            val source = sources.new(
                    snapshot.name, snapshot.code,
                    mutableListOf(location))

            listener.expectNext.containsExactly(SourceSavedEvent(
                    null, source))

            source.update {
                save()
            }

            listener.expectNext.isEmpty()
        }
    }
}
