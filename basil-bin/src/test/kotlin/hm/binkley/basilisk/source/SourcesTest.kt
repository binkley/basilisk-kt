package hm.binkley.basilisk.source

import ch.tutteli.atrium.api.cc.en_GB.containsExactly
import ch.tutteli.atrium.api.cc.en_GB.toBe
import ch.tutteli.atrium.verbs.expect
import hm.binkley.basilisk.db.testTransaction
import io.micronaut.context.event.ApplicationEventListener
import io.micronaut.test.annotation.MicronautTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import javax.inject.Inject
import javax.inject.Singleton

@Singleton // TODO: How to make this 'object', not 'class'?
internal class TestListener : ApplicationEventListener<SourceSavedEvent> {
    private val _received = mutableListOf<SourceSavedEvent>()
    val received
        get() = _received

    override fun onApplicationEvent(event: SourceSavedEvent) {
        _received.add(event)
    }
}

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
    lateinit var listener: TestListener

    @AfterEach
    fun tearDown() {
        listener.received.clear()
    }

    @Test
    fun shouldFindNoSource() {
        testTransaction {
            val source = sources.source(code)

            expect(source).toBe(null)
        }
    }

    @Test
    fun shouldRoundTrip() {
        testTransaction {
            sources.create(name, code)
            val source = sources.source(code)!!

            expect(source.code).toBe(code)
            expect(source.name).toBe(name)
        }
    }

    @Test
    fun shouldPublishSaveEvents() {
        val name = name
        val code = code

        testTransaction {
            val record = SourceRecord.new {
                this.name = name
                this.code = code
            }
            val source = Source(record, sources)

            source.save()

            expect(listener.received).containsExactly(
                    SourceSavedEvent(source))
        }
    }
}
