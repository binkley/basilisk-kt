package hm.binkley.basilisk.chef

interface Chefs {
    companion object {
        // TODO: Consider types instead of status fields:
        // - FitChef vs UnfitChef, etc.
        const val FIT = "FIT"
    }

    fun all(): Iterable<Chef>

    fun byCode(code: String): Chef?

    fun new(chef: ChefResource): Chef
}

interface ChefDetails {
    val code: String
    val name: String
    val health: String
}

interface MutableChefDetails {
    val code: String
    var name: String
    var health: String
}

interface Chef : ChefDetails {
    fun update(block: MutableChef.() -> Unit): Chef
}

interface MutableChef : MutableChefDetails {
    fun save(): MutableChef

    fun delete()
}
