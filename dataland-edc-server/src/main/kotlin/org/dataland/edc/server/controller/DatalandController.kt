package org.dataland.edc.server.controller

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import jakarta.ws.rs.core.Response
import org.dataland.edc.server.api.DatalandEDCApi
import org.dataland.edc.server.service.DataManager
import org.eclipse.dataspaceconnector.spi.system.ServiceExtensionContext

class DatalandController(
    private val dataManager: DataManager,
    private val context: ServiceExtensionContext
    ): DatalandEDCApi {

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
        val splitDataId = dataId.split(":")
        if (splitDataId.size != 2) throw IllegalArgumentException("The data ID $dataId has an invalid format.")
        return dataManager.retrieveAssetFromTrustee(
            assetId = splitDataId[0],
            contractDefinitionId = splitDataId[1]
        )
    }

    override fun storeReceivedData(id: String, data: ByteArray): Response {
        context.monitor.info("Received asset POST request by Trustee with ID $id.")
        val decodedData: Map<String, String> = objectMapper.readValue(data.decodeToString())
        dataManager.storeReceivedAsset(id, decodedData["content"]!!)
        return Response.ok("Dataland-connector received asset with asset ID $id").build()
    }
}
