package hm.binkley.basilisk.chef

import ch.tutteli.atrium.api.cc.en_GB.isEmpty
import ch.tutteli.atrium.api.cc.en_GB.toBe
import ch.tutteli.atrium.verbs.expect
import io.micronaut.test.annotation.MicronautTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import javax.inject.Inject

@MicronautTest
@TestInstance(PER_CLASS)
internal class RemoteChefsTest {
    companion object {
        const val name = "CHEF BOB"
        const val code = "CHEF123"
    }

    @Inject
    lateinit var chefs: RemoteChefs
    @Inject
    lateinit var mock: MockChefsClient

    @BeforeEach
    fun tearDown() = mock.reset()

    @Test
    fun `should find no chefs`() {
        val chefs = chefs.all()

        expect(chefs).isEmpty()
    }

    @Test
    fun `should find some chefs`() {
        val chefs = listOf(
                ChefResource("BOB", "Chef Bob"),
                ChefResource("NANCY", "Chef Nancy"))
        mock.all = chefs

        val body = this.chefs.all()

        expect(body).toBe(chefs.map {
            this.chefs.from(it)
        })
    }

    @Test
    fun `should find no chef`() {
        val chef = chefs.byCode(code)

        expect(chef).toBe(null)
    }

    @Test
    fun `should find a chef`() {
        val one = ChefResource(code, name)
        mock.one = one

        val chef = chefs.byCode(code)

        expect(chef).toBe(this.chefs.from(one))
    }
}
