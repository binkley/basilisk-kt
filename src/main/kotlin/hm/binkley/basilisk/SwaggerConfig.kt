package hm.binkley.basilisk

import io.micronaut.context.annotation.ConfigurationProperties
import io.micronaut.core.annotation.Introspected

@ConfigurationProperties("swagger")
@Introspected
class SwaggerConfig {
    var version: String? = null
    var layout: String? = null
    var deepLinking = false
    var displayRequestDuration = false
    var showCommonExtensions = false
    var showExtensions = false
    var urls: List<URIConfig?>? =
            null

    @ConfigurationProperties("urls")
    class URIConfig {
        var name: String? = null
        var url: String? = null
    }
}
