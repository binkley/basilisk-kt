package hm.binkley.basilisk

import ch.tutteli.atrium.api.cc.en_GB.toBe
import ch.tutteli.atrium.verbs.expect
import io.micronaut.http.HttpStatus.OK
import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.test.annotation.MicronautTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import java.net.URI
import java.net.http.HttpClient.newHttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers.discarding
import javax.inject.Inject

@MicronautTest
@TestInstance(PER_CLASS)
internal class ApplicationTest {
    @Inject
    lateinit var server: EmbeddedServer

    private val client = newHttpClient()

    @Test
    fun `should keep metrics`() {
        val request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create("http://localhost:${server.port}/admin/metrics"))
                .build();

        val response = client.send(request, discarding())

        expect(response.statusCode()).toBe(OK.code);
    }
}
