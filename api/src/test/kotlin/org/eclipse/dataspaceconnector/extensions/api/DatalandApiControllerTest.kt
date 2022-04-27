package org.eclipse.dataspaceconnector.extensions.api

import org.eclipse.dataspaceconnector.spi.monitor.Monitor
import org.junit.jupiter.api.Test


class DatalandApiControllerTest {

    val datalandApiController =
        DatalandApiController(
            monitor = Monitor(),
            consumerApiController = ConsumerApiController()
    )

    @Test
    fun `asfasf a` () {
        val testData = "Content: 10"
        datalandApiController.insertData(testData)
    }
}