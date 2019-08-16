package hm.binkley.basilisk.chef

import io.micronaut.http.annotation.Controller

@Controller
class ChefsController(private val client: RemoteChefs)
    : ChefsOperations by client
