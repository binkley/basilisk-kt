package hm.binkley.basilisk.chef

interface Chefs {
    companion object {
        const val FIT = "FIT"
    }

    fun all(): Iterable<Chef>

    fun byCode(code: String): Chef?

    fun new(chef: ChefResource): Chef
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

interface Chef : ChefDetails {
    fun update(block: MutableChef.() -> Unit): Chef
}

interface MutableChef : MutableChefDetails {
    fun save(): MutableChef

    fun delete()
}
