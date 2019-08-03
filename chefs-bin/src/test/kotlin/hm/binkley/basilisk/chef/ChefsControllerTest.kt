package hm.binkley.basilisk.chef

import ch.tutteli.atrium.api.cc.en_GB.isEmpty
import ch.tutteli.atrium.verbs.expect
import io.micronaut.core.type.Argument
import io.micronaut.http.HttpRequest.GET
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.annotation.MicronautTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import javax.inject.Inject

@MicronautTest
@TestInstance(PER_CLASS)
internal class ChefsControllerTest {
    @Inject
    @field:Client("/chefs")
    lateinit var client: HttpClient

    @Test
    fun `should get all chefs`() {
        val body = client.toBlocking().retrieve(
                GET<Any>("/"),
                Argument.listOf(ChefResource::class.java))

        expect(body).isEmpty()
    }
}
