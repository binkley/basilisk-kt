package hm.binkley.basilisk.chef

import hm.binkley.basilisk.chef.Chefs.Companion.FIT
import io.micronaut.context.event.ApplicationEvent

interface Chefs {
    companion object {
        const val FIT = "FIT"
    }

    fun all(): Iterable<Chef>

    fun byCode(code: String): Chef?

    /** Saves a new chef in [FIT] health. */
    fun new(name: String, code: String, health: String = FIT): Chef
}

interface ChefDetails {
    val name: String
    val code: String
    val health: String
}

interface MutableChefDetails {
    var name: String
    var code: String
    var health: String
}

data class ChefSavedEvent(
        val before: ChefResource?,
        val after: Chef?) : ApplicationEvent(after ?: before)

interface Chef : ChefDetails {
    fun update(block: MutableChef.() -> Unit): Chef
}

interface MutableChef : MutableChefDetails {
    fun save(): MutableChef

    fun delete()
}
