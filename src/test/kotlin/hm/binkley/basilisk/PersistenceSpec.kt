package hm.binkley.basilisk

import ch.tutteli.atrium.api.cc.en_GB.containsExactly
import ch.tutteli.atrium.verbs.expect
import com.zaxxer.hikari.util.DriverDataSource
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Replaces
import io.micronaut.test.annotation.MicronautTest
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.testcontainers.containers.PostgreSQLContainerProvider
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.util.*

@MicronautTest
@TestInstance(PER_CLASS)
@Testcontainers
class PersistenceSpec {
    companion object {
        @Container
        val container = PostgreSQLContainerProvider().newInstance()
    }

    @Bean
    @Replaces
    fun testDataSource() = DriverDataSource(container.getJdbcUrl(),
            "org.postgresql.Driver",
            Properties(),
            container.getUsername(),
            container.getPassword())

    @Test
    fun shouldRoundTripSimple() {
        transaction {
            val chef = ChefRecord.new {
                name = "CHEF BOB"
            }
            chef.flush()
            val chefs = ChefRecord.all()
            expect(chefs).containsExactly(chef)

            rollback() // TODO: Integrate with @MicronautTest rollbacks
        }
    }

    @Test
    fun shouldRoundTripComplex() {
        transaction {
            val chef = ChefRecord.new {
                name = "CHEF BOB"
            }
            val recipe = RecipeRecord.new {
                name = "TASTY STEW"
                this.chef = chef
            }
            recipe.flush()
            val chefs = ChefRecord.all()
            expect(chefs).containsExactly(chef)
            val recipes = RecipeRecord.all()
            expect(recipes).containsExactly(recipe)

            rollback() // TODO: Integrate with @MicronautTest rollbacks
        }
    }
}
