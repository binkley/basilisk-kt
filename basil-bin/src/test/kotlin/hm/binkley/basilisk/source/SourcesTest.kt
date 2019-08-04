package hm.binkley.basilisk.source

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
internal class SourcesTest {
    companion object {
        const val name = "RHUBARB"
        const val code = "SRC012"
    }

    @Inject
    lateinit var sources: Sources
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
        testTransaction {
            val firstSnapshot = SourceResource(name, code, listOf())
            val secondSnapshot = SourceResource("LIME", code, listOf())

            val source = sources.new(firstSnapshot.name, firstSnapshot.code)

            expect(listener.received).containsExactly(SourceSavedEvent(
                    null, source))
            listener.reset()

            source.update {
                this.name = secondSnapshot.name
                save()
            }

            expect(listener.received).containsExactly(SourceSavedEvent(
                    firstSnapshot, source))
            listener.reset()

            source.update {
                delete()
            }

            expect(listener.received).containsExactly(SourceSavedEvent(
                    secondSnapshot, null))
        }
    }
}
