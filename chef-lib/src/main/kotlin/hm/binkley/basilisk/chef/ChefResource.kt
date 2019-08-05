package hm.binkley.basilisk.chef

data class ChefResource(
        val name: String, val code: String, val health: String) {
    constructor(chef: Chef) : this(chef.name, chef.code, chef.health)
}
