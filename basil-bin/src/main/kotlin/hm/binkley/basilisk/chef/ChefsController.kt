package hm.binkley.basilisk.chef

import io.micronaut.http.annotation.Controller

@Controller("/chefs")
class ChefsController(private val client: ChefClient)
    : ChefsOperations by client
