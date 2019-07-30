package hm.binkley.basilisk.chef

import ch.tutteli.atrium.api.cc.en_GB.containsExactly
import ch.tutteli.atrium.verbs.expect
import hm.binkley.basilisk.db.testTransaction
import io.micronaut.test.annotation.MicronautTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS

/**
 * @todo Why all the nestiness?
 * @todo Is there a nicer way to rollback each test than `testTransaction`?
 */
@MicronautTest
@TestInstance(PER_CLASS)
internal class ChefsPersistenceTest {
    @Test
    fun shouldRoundTrip() {
        testTransaction {
            val chef = ChefRecord.new {
                name = "CHEF BOB"
                code = "CHEF123"
            }
            chef.flush()

            val chefs = ChefRecord.all()

            expect(chefs).containsExactly(chef)
        }
    }
}
