package org.dataland.datalandbackend.edcClient

import org.dataland.datalandbackend.edcClient.api.DefaultApi
import org.dataland.datalandbackend.edcClient.infrastructure.ApiClient
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * This class holds the configuration properties and the beans to auto-configure
 * this library in a spring-boot environment
 */
@Configuration
@ConfigurationProperties(prefix = "dataland-edc-client")
open class Configuration {
    var baseUrl: String? = null

    /**
     * The bean to configure the EDCConnectorInterface
     */
    @Bean
    open fun getApiClient(): ApiClient {
        return DefaultApi(
            basePath = baseUrl!!
        )
    }
}
