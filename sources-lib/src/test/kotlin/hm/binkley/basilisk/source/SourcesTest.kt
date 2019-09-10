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
import org.junit.jupiter.api.BeforeEach
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
    lateinit var listener: TestListener<SourceChangedEvent>

    @BeforeEach
    fun setUp() {
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
            sources.new(code, name)
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
                    LocationResource(locationACode, locationAName)).update {
                save()
            }
            val locationB = locations.new(
                    LocationResource(locationBCode, locationBName)).update {
                save()
            }
            listener.reset()

            val firstSnapshot = SourceResource(code, name, listOf(
                    LocationResource(locationA),
                    LocationResource(locationB)))
            val secondSnapshot = SourceResource(code, "LIME", listOf(
                    LocationResource(locationA)))
            val thirdSnapshot = SourceResource(code, "LIME", listOf())

            val source = sources.new(
                    firstSnapshot.code, firstSnapshot.name,
                    mutableListOf(locationA, locationB))

            listener.expectNext.containsExactly(SourceChangedEvent(
                    null, SourceResource(source)))

            source.update {
                this.name = secondSnapshot.name
                this.locations.remove(locationB)
                save()
            }

            listener.expectNext.containsExactly(SourceChangedEvent(
                    firstSnapshot, SourceResource(source)))

            source.update {
                this.locations = mutableListOf()
                save()
            }

            listener.expectNext.containsExactly(SourceChangedEvent(
                    secondSnapshot, SourceResource(source)))

            source.update {
                delete()
            }

            listener.expectNext.containsExactly(SourceChangedEvent(
                    thirdSnapshot, null))
        }
    }

    @Test
    fun shouldSkipPublishSaveEventsIfUnchangd() {
        val locationName = "The Dallas Yellow Rose"
        val locationCode = "DAL"

        testTransaction {
            val location = locations.new(
                    LocationResource(locationCode, locationName))
            listener.reset()

            val snapshot = SourceResource(
                    code, name, listOf(LocationResource(location)))

            val source = sources.new(
                    snapshot.code, snapshot.name,
                    mutableListOf(location))

            listener.expectNext.containsExactly(SourceChangedEvent(
                    null, SourceResource(source)))

            source.update {
                save()
            }

            listener.expectNext.isEmpty()
        }
    }
}
