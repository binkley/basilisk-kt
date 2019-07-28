package hm.binkley.basilisk

import ch.tutteli.atrium.api.cc.en_GB.toBe
import ch.tutteli.atrium.verbs.expect
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.micronaut.http.HttpStatus.OK
import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.test.annotation.MicronautTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import java.net.URI
import java.net.http.HttpClient.newHttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers
import java.net.http.HttpResponse.BodyHandlers.discarding
import javax.inject.Inject

@MicronautTest
@TestInstance(PER_CLASS)
internal class BasiliskApplicationTest {
    @Inject
    lateinit var server: EmbeddedServer
    @Inject
    lateinit var objectMapper: ObjectMapper

    private val client = newHttpClient()

    @Test
    fun `should be healthy`() {
        val request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(
                        "http://localhost:${server.port}/admin/health"))
                .build();

        val response = client.send(request, discarding())

        expect(response.statusCode()).toBe(OK.code);
    }

    @Test
    fun `should be informative`() {
        val request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create("http://localhost:${server.port}/admin/info"))
                .build();

        val response = client.send(request, BodyHandlers.ofByteArray())

        expect(response.statusCode()).toBe(OK.code);

        // TODO: Parsing fails on null nodes -- check this is true
        objectMapper.readValue<Info>(response.body())
    }

    data class Info(val build: JsonNode, val git: JsonNode)

    @Test
    fun `should keep metrics`() {
        val request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(
                        "http://localhost:${server.port}/admin/metrics"))
                .build();

        val response = client.send(request, discarding())

        expect(response.statusCode()).toBe(OK.code);
    }

    @Test
    fun `should describe API`() {
        val request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(
                        "http://localhost:${server.port}/api-docs/swagger/basilisk-0.yml"))
                .build();

        val response = client.send(request, discarding())

        expect(response.statusCode()).toBe(OK.code);
    }

    @Test
    fun `should let user interact with API`() {
        val request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(
                        "http://localhost:${server.port}/swagger"))
                .build();

        val response = client.send(request, discarding())

        expect(response.statusCode()).toBe(OK.code);
    }
}
