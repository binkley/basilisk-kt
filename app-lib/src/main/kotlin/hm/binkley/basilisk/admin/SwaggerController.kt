package hm.binkley.basilisk.admin

import hm.binkley.basilisk.admin.SwaggerConfig.URIConfig
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.views.View
import io.swagger.v3.oas.annotations.Hidden
import javax.inject.Inject
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

@Controller("/swagger")
@Hidden
open class SwaggerController {
    @Inject
    internal var config: SwaggerConfig? = null

    @View("swagger/index")
    @Get
    open fun index(): SwaggerConfig? {
        return config
    }

    @View("swagger/index")
    @Get("/{url}")
    open fun renderSpec(@NotNull url: String?): SwaggerConfig {
        val config = SwaggerConfig()
        config.deepLinking = this.config?.deepLinking ?: false
        config.layout = this.config?.layout
        val uriConfig = URIConfig()
        uriConfig.name = url // TODO: Why not "name"?
        uriConfig.url = url
        config.urls = listOf(uriConfig)
        return config
    }

    @View("swagger/index")
    @Post
    open fun renderSpecs(@Body @NotEmpty urls: List<URIConfig?>?)
            : SwaggerConfig {
        val config = SwaggerConfig()
        config.deepLinking = this.config?.deepLinking ?: false
        config.layout = this.config?.layout
        config.urls = urls
        return config
    }
}
