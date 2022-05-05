package org.dataland.edc.server.controller

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import jakarta.ws.rs.core.Response
import org.dataland.edc.server.api.DatalandEDCApi
import org.dataland.edc.server.service.DataManager
import org.eclipse.dataspaceconnector.spi.system.ServiceExtensionContext

/**
 * Implementation of the Dataland EDC Api
 * @param dataManager the in memory data manager orchestrating the required tasks
 * @param context the context containing constants and the monitor for logging
 */
class DatalandController(
    private val dataManager: DataManager,
    private val context: ServiceExtensionContext
) : DatalandEDCApi {

    private val objectMapper = jacksonObjectMapper()

    override fun checkHealth(): String {
        context.monitor.info("Received a health request.")
        return objectMapper.writeValueAsString(mapOf("response" to "I am alive!"))
    }

    override fun insertData(data: String): String {
        context.monitor.info("Received data to store in the trustee.")
        return dataManager.provideAssetToTrustee(data)
    }

    override fun provideAsset(providerAssetId: String): String {
        context.monitor.info("Asset with provider asset ID $providerAssetId is requested.")
        return dataManager.getProvidedAsset(providerAssetId)
    }

    override fun selectDataById(dataId: String): String {
        context.monitor.info("Asset with data ID $dataId is requested.")
        return dataManager.getDataById(dataId)
    }

    override fun storeReceivedData(assetId: String, data: ByteArray): Response {
        context.monitor.info("Received asset POST request by Trustee with ID $assetId.")
        val decodedData: Map<String, String> = objectMapper.readValue(data.decodeToString())
        dataManager.storeReceivedAsset(assetId, decodedData["content"]!!)
        return Response.ok("Dataland-connector received asset with asset ID $assetId").build()
    }
}
