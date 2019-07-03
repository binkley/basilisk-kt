package x.micronaut

import ch.tutteli.atrium.api.cc.en_GB.toBe
import ch.tutteli.atrium.verbs.expect
import io.micronaut.test.annotation.MicronautTest
import org.junit.jupiter.api.Test
import javax.inject.Inject

@MicronautTest
class HelloClientSpec {
    @Inject
    lateinit var client: HelloClient

    @Test
    fun `should greet the world`() {
        expect(client.greet(HelloRequest("World")))
                .toBe(HelloResponse("Hello, World!"))
    }
}
