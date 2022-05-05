package org.dataland.edc.server.controller

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import jakarta.ws.rs.core.Response
import org.dataland.edc.server.api.DatalandEurodatApi
import org.dataland.edc.server.service.DataManager
import org.eclipse.dataspaceconnector.spi.system.ServiceExtensionContext

/**
 * Implementation of the Dataland EDC Api
 * @param dataManager the in memory data manager orchestrating the required tasks
 * @param context the context containing constants and the monitor for logging
 */
class DatalandEurodatController(
    private val dataManager: DataManager,
    private val context: ServiceExtensionContext
) : DatalandEurodatApi {

    private val objectMapper = jacksonObjectMapper()

    override fun provideAsset(datalandAssetId: String): String {
        context.monitor.info("Asset with dataland asset ID $datalandAssetId is requested.")
        return dataManager.getProvidedAsset(datalandAssetId)
    }

    override fun storeReceivedData(eurodatAssetId: String, data: ByteArray): Response {
        context.monitor.info("Received asset POST request by EuroDaT with ID $eurodatAssetId.")
        val decodedData: Map<String, String> = objectMapper.readValue(data.decodeToString())
        dataManager.storeReceivedAsset(eurodatAssetId, decodedData["content"]!!)
        return Response.ok("Dataland-connector received asset with asset ID $eurodatAssetId").build()
    }
}
