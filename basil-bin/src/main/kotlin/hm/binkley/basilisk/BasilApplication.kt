package hm.binkley.basilisk

import io.micronaut.runtime.Micronaut
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info

@OpenAPIDefinition(info = Info(
        // TODO: Pick a diff title, leave Swagger YML file name alone
        title = "Basil",
        version = "0"))
object BasilApplication {
    @JvmStatic
    fun main(args: Array<String>) {
        Micronaut.build()
                .packages("hm.binkley.basilisk")
                .mainClass(BasilApplication.javaClass)
                .start()
    }
}
