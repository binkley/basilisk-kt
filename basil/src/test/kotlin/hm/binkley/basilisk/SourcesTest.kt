package hm.binkley.basilisk

import ch.tutteli.atrium.api.cc.en_GB.toBe
import ch.tutteli.atrium.verbs.expect
import io.micronaut.test.annotation.MicronautTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import javax.inject.Inject

@MicronautTest
@TestInstance(PER_CLASS)
internal class SourcesTest {
    companion object {
        val name = "RHUBARB"
        val code = "SRC012"
    }

    @Inject
    lateinit var sources: Sources

    @Test
    fun shouldFindNoSource() {
        testTransaction {
            val ingredient = sources.source(code)

            expect(ingredient).toBe(null)
        }
    }

    @Test
    fun shouldRoundTrip() {
        testTransaction {
            sources.create(name, code)
            val source = sources.source(code)

            expect(source!!.code).toBe(code)
            expect(source.name).toBe(name)
        }
    }
}
