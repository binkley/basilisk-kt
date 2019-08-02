package hm.binkley.basilisk.chef

import ch.tutteli.atrium.api.cc.en_GB.containsExactly
import ch.tutteli.atrium.api.cc.en_GB.toBe
import ch.tutteli.atrium.verbs.expect
import hm.binkley.basilisk.db.testTransaction
import io.micronaut.context.event.ApplicationEventListener
import io.micronaut.test.annotation.MicronautTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import javax.inject.Inject
import javax.inject.Singleton

@Singleton // TODO: How to make this 'object', not 'class'?
internal class TestListener : ApplicationEventListener<ChefSavedEvent> {
    private val _received = mutableListOf<ChefSavedEvent>()
    val received
        get() = _received

    override fun onApplicationEvent(event: ChefSavedEvent) {
        _received.add(event)
    }
}

@MicronautTest
@TestInstance(PER_CLASS)
internal class ChefsTest {
    companion object {
        const val name = "CHEF BOB"
        const val code = "CHEF123"
    }

    @Inject
    lateinit var chefs: Chefs
    @Inject
    lateinit var listener: TestListener

    @AfterEach
    fun tearDown() {
        listener.received.clear()
    }

    @Test
    fun shouldFindNoChef() {
        testTransaction {
            val ingredient = chefs.chef(code)

            expect(ingredient).toBe(null)
        }
    }

    @Test
    fun shouldRoundTrip() {
        testTransaction {
            chefs.create(name, code)
            val chef = chefs.chef(code)!!

            expect(chef.code).toBe(code)
            expect(chef.name).toBe(name)
        }
    }

    @Test
    fun shouldPublishSaveEvents() {
        val name = name
        val code = code

        testTransaction {
            val record = ChefRecord.new {
                this.name = name
                this.code = code
            }
            val chef = Chef(record, chefs)

            chef.save()

            expect(listener.received).containsExactly(
                    ChefSavedEvent(chef))
        }
    }
}
