package hm.binkley.basilisk.chef

import io.micronaut.http.annotation.Controller

@Controller
class RemoteChefsController(private val client: ChefsClient)
    : ChefsOperations by client
