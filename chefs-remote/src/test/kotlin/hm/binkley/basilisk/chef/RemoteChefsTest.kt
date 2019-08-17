package hm.binkley.basilisk.chef

import ch.tutteli.atrium.api.cc.en_GB.toBe
import ch.tutteli.atrium.verbs.expect
import io.micronaut.test.annotation.MicronautTest
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

    @Test
    fun shouldFindNoChef() {
        val chef = chefs.byCode(code)

        expect(chef).toBe(null)
    }
}
