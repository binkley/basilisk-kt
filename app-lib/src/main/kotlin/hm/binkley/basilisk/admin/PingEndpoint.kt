package hm.binkley.basilisk.admin

import io.micronaut.core.annotation.Introspected
import io.micronaut.health.HealthStatus
import io.micronaut.management.endpoint.annotation.Endpoint
import io.micronaut.management.endpoint.annotation.Read

@Endpoint(id = "ping", defaultSensitive = false)
class PingEndpoint {
    companion object {
        val status = PingResult()
    }

    @Read
    fun ping() = status

    @Introspected
    class PingResult {
        val status = HealthStatus.NAME_UP
    }
}
