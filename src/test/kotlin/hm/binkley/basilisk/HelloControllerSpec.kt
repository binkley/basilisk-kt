package hm.binkley.basilisk

import ch.tutteli.atrium.api.cc.en_GB.toBe
import ch.tutteli.atrium.verbs.expect
import io.micronaut.http.HttpRequest.POST
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.test.annotation.MicronautTest
import org.junit.jupiter.api.Test
import javax.inject.Inject

@MicronautTest
class HelloControllerSpec {
    @Inject
    lateinit var server: EmbeddedServer

    @Inject
    @field:Client("/")
    lateinit var client: HttpClient

    @Test
    fun `should greet the world`() {
        val body = client.toBlocking().retrieve(
                POST("/hello", HelloRequest("World")),
                HelloResponse::class.java)

        expect(body).toBe(HelloResponse("Hello, World!"))
    }
}
