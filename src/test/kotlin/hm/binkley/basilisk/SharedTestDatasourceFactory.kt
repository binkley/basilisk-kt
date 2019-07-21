package hm.binkley.basilisk

import io.micronaut.configuration.jdbc.hikari.DatasourceConfiguration
import io.micronaut.configuration.jdbc.hikari.DatasourceFactory
import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Context
import io.micronaut.context.annotation.EachBean
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Replaces
import java.util.concurrent.ConcurrentHashMap
import javax.annotation.PreDestroy
import javax.sql.DataSource

@Factory
@Replaces(DatasourceFactory::class)
class SharedTestDatasourceFactory(context: ApplicationContext)
    : DatasourceFactory(context) {
    companion object {
        private var cache = ConcurrentHashMap<String, DataSource>(1)

        fun dataSource(jdbcUrl: String,
                new: (jdbcUrl: String) -> DataSource) =
                cache.computeIfAbsent(jdbcUrl, new)
    }

    @Context
    @EachBean(DatasourceConfiguration::class)
    override fun dataSource(configuration: DatasourceConfiguration) =
            dataSource(configuration.jdbcUrl) {
                super.dataSource(configuration)
            }

    @PreDestroy
    override fun close() {
        // Do not close -- share the ephemeral database for tests
    }
}
