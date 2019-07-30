package hm.binkley.basilisk.chef

import io.micronaut.runtime.Micronaut
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info

@OpenAPIDefinition(info = Info(
        title = "Chefs",
        version = "0"))
object ChefsApplication {
    @JvmStatic
    fun main(args: Array<String>) {
        Micronaut.build()
                .packages("hm.binkley.basilisk")
                .mainClass(ChefsApplication.javaClass)
                .start()
    }
}
