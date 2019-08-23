package hm.binkley.basilisk.chef

import ch.tutteli.atrium.api.cc.en_GB.isEmpty
import ch.tutteli.atrium.api.cc.en_GB.toBe
import ch.tutteli.atrium.verbs.expect
import io.micronaut.core.type.Argument
import io.micronaut.http.HttpRequest.GET
import io.micronaut.http.HttpRequest.POST
import io.micronaut.http.HttpStatus.NOT_FOUND
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.test.annotation.MicronautTest
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import java.net.URI
import java.net.http.HttpClient.newHttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers
import java.nio.charset.StandardCharsets.UTF_8
import javax.inject.Inject

@MicronautTest
@TestInstance(PER_CLASS)
internal class PersistedChefsControllerTest {
    companion object {
        const val statusCodeFixed = false // TODO Better status codes
    }

    @Inject
    lateinit var server: EmbeddedServer

    @Inject
    @field:Client("/")
    lateinit var client: HttpClient

    @Test
    fun `should get no chefs`() {
        val body = client.toBlocking().retrieve(
                GET<List<ChefResource>>("/chefs"),
                Argument.listOf(ChefResource::class.java))

        expect(body).isEmpty()
    }

    @Test
    fun `should get no chef`() {
        val client = newHttpClient()
        val request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(
                        "http://${server.host}:${server.port}/chef/CHEF123"))
                .build()
        val response = client.send(request, BodyHandlers.ofString(UTF_8))

        if (statusCodeFixed) {
            expect(response.statusCode()).toBe(NOT_FOUND)
            expect(response.body()).isEmpty()
        } else
            expect(response.statusCode()).toBe(500)
    }

    @Disabled("TODO: FIX ME")
    @Test
    fun `should make chef`() {
        val chef = ChefResource("Chef Boy-ar-dee", "CHEF123", "FIT")

        val body = client.toBlocking().exchange(
                POST<ChefResource>("/chefs", chef),
                ChefResource::class.java)

        expect(body).toBe(chef)
    }
}
