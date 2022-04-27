package org.eclipse.dataspaceconnector.extensions.controller

import org.junit.jupiter.api.Test


class DatalandApiControllerTest {

    val datalandController = DatalandController()

    @Test
    fun `asfasf a` () {
        val testData = "Content: 10"
        datalandController.registerDataAsAsset(testData)
    }
}