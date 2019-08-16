package hm.binkley.basilisk.chef

import ch.tutteli.atrium.api.cc.en_GB.isEmpty
import ch.tutteli.atrium.api.cc.en_GB.toBe
import ch.tutteli.atrium.verbs.expect
import io.micronaut.http.HttpHeaders.LOCATION
import io.micronaut.http.HttpStatus.CREATED
import io.micronaut.test.annotation.MicronautTest
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import javax.inject.Inject

@MicronautTest
@TestInstance(PER_CLASS)
internal class ChefsControllerTest {
    @Inject
    lateinit var client: ChefsClient

    @Test
    fun `should get no chefs`() {
        val body = client.all()

        expect(body.toList()).isEmpty()
    }

    @Test
    fun `should get no chef`() {
        val body = client.byCode("CHEF123")

        expect(body).toBe(null)
    }

    @Disabled("TODO: FIX ME")
    @Test
    fun `should make chef`() {
        val chef = ChefResource("Chef Boy-ar-dee", "CHEF123", "FIT")
        val response = client.new(chef)

        expect(response.status).toBe(CREATED)
        expect(response.header(LOCATION)).toBe("/chef/${chef.code}")
        expect(response.body()).toBe(chef)
    }
}
