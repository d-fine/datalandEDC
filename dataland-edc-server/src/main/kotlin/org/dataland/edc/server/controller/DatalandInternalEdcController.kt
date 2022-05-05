package org.dataland.edc.server.controller

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.dataland.edc.server.api.DatalandInternalEdcApi
import org.dataland.edc.server.service.DataManager
import org.eclipse.dataspaceconnector.spi.system.ServiceExtensionContext

/**
 * Implementation of the Dataland EDC Api
 * @param dataManager the in memory data manager orchestrating the required tasks
 * @param context the context containing constants and the monitor for logging
 */
class DatalandInternalEdcController(
    private val dataManager: DataManager,
    private val context: ServiceExtensionContext
) : DatalandInternalEdcApi {

    private val objectMapper = jacksonObjectMapper()

    override fun checkHealth(): String {
        context.monitor.info("Received a health request.")
        return objectMapper.writeValueAsString(mapOf("response" to "I am alive!"))
    }

    override fun insertData(data: String): Map<String, String> {
        context.monitor.info("Received data to store in the trustee.")
        return mapOf("dataId" to dataManager.provideAssetToTrustee(data))
    }

    override fun selectDataById(dataId: String): String {
        context.monitor.info("Asset with data ID $dataId is requested.")
        return dataManager.getDataById(dataId)
    }
}
