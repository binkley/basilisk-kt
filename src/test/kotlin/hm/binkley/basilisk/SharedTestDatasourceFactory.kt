package hm.binkley.basilisk

import io.micronaut.configuration.jdbc.hikari.DatasourceConfiguration
import io.micronaut.configuration.jdbc.hikari.DatasourceFactory
import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Context
import io.micronaut.context.annotation.EachBean
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Replaces
import javax.annotation.PreDestroy
import javax.sql.DataSource

@Factory
@Replaces(DatasourceFactory::class)
class SharedTestDatasourceFactory(context: ApplicationContext)
    : DatasourceFactory(context) {
    companion object {
        private var cached: DataSource? = null
    }

    @Context
    @EachBean(DatasourceConfiguration::class)
    override fun dataSource(
            datasourceConfiguration: DatasourceConfiguration?): DataSource {
        if (null == cached) {
            cached = super.dataSource(datasourceConfiguration)
        }

        return cached!!
    }

    @PreDestroy
    override fun close() {
        // Do not close -- share the ephemeral database for tests
    }
}
