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
class HelloClientSpec {
    @Inject
    lateinit var client: HelloClient

    @Test
    fun `should greet the world`() {
        expect(client.greet(HelloRequest("World")))
                .toBe(HelloResponse("Hello, World!"))
    }
}
