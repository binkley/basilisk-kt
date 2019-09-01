package hm.binkley.basilisk.chef

import ch.tutteli.atrium.api.cc.en_GB.isEmpty
import ch.tutteli.atrium.api.cc.en_GB.toBe
import ch.tutteli.atrium.verbs.expect
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.micronaut.http.HttpStatus.CREATED
import io.micronaut.http.HttpStatus.NOT_FOUND
import io.micronaut.http.HttpStatus.OK
import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.test.annotation.MicronautTest
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import java.net.URI
import java.net.http.HttpClient.newHttpClient
import java.net.http.HttpRequest
import java.net.http.HttpRequest.BodyPublishers
import java.net.http.HttpResponse
import java.net.http.HttpResponse.BodyHandlers
import java.net.http.HttpResponse.BodyHandlers.discarding
import java.nio.charset.StandardCharsets.UTF_8
import javax.inject.Inject

@MicronautTest
@TestInstance(PER_CLASS)
internal class PersistedChefsControllerTest {
    @Inject
    lateinit var server: EmbeddedServer
    @Inject
    lateinit var objectMapper: ObjectMapper

    @Test
    fun `should get no chefs`() {
        val client = newHttpClient()
        val response = client.send(
                GET("/chefs"), BodyHandlers.ofString(UTF_8))

        expect(response.statusCode()).toBe(OK.code)

        val body = response.readBody<List<ChefResource>>()
        expect(body).isEmpty()
    }

    @Test
    fun `should get no chef`() {
        val client = newHttpClient()
        val response = client.send(
                GET("/chef/CHEF123"), discarding())

        expect(response.statusCode()).toBe(NOT_FOUND.code)
    }

    @Test
    fun `should delete no chef`() {
        val client = newHttpClient()
        val response = client.send(
                DELETE("/chef/CHEF123"), discarding())

        expect(response.statusCode()).toBe(NOT_FOUND.code)
    }

    @Disabled("How does POST work?  Why is it not a method?")
    @Test
    fun `should create a chef`() {
        val chef = ChefResource("CHEF123", "Chef Boy-ar-dee")
        val client = newHttpClient()
        val response = client.send(
                POST("/chefs", chef), BodyHandlers.ofString(UTF_8))

        expect(response.statusCode()).toBe(CREATED.code)
        println(response.headers().map())

        val body = response.readBody<ChefResource>()
        expect(body).toBe(chef)
    }

    private fun GET(path: String) = HttpRequest.newBuilder()
            .GET()
            .uri(uri(path))
            .build()

    private fun DELETE(path: String) = HttpRequest.newBuilder()
            .DELETE()
            .uri(uri(path))
            .build()

    private fun POST(path: String, body: Any) = HttpRequest.newBuilder()
            .POST(body.writeBody())
            .uri(uri(path))
            .build()

    private fun uri(path: String) =
            URI.create("http://${server.host}:${server.port}$path")

    private fun Any.writeBody() = BodyPublishers.ofString(
            objectMapper.writeValueAsString(this), UTF_8)

    private inline fun <reified T> HttpResponse<String>.readBody() =
            objectMapper.readValue<T>(body())
}
