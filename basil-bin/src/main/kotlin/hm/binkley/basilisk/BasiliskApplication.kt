package hm.binkley.basilisk

import io.micronaut.runtime.Micronaut
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info

@OpenAPIDefinition(info = Info(
        title = "Basilisk",
        version = "0"))
object BasiliskApplication {
    @JvmStatic
    fun main(args: Array<String>) {
        Micronaut.build()
                .packages("kotlin.micronaut")
                .mainClass(BasiliskApplication.javaClass)
                .start()
    }
}
