package org.dataland.edc.server.controller

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import jakarta.ws.rs.core.Response
import org.dataland.edc.server.api.DatalandEurodatApi
import org.dataland.edc.server.service.EuroDaTAssetCache
import org.dataland.edc.server.service.LocalAssetStore
import org.eclipse.dataspaceconnector.spi.system.ServiceExtensionContext

/**
 * Implementation of the Dataland EDC Api
 * @param localAssetStore a store of the assets provided to EuroDaT
 * @param euroDaTAssetCache a cache of assets provided by EuroDaT
 * @param context the context containing constants and the monitor for logging
 */
class DatalandEurodatController(
    private val context: ServiceExtensionContext,
    private val localAssetStore: LocalAssetStore,
    private val euroDaTAssetCache: EuroDaTAssetCache
) : DatalandEurodatApi {

    private val objectMapper = jacksonObjectMapper()

    override fun provideAsset(datalandAssetId: String): String {
        context.monitor.info("Asset with dataland asset ID $datalandAssetId is requested.")
        val returnValue = localAssetStore.retrieveFromStore(datalandAssetId) ?: ""
        localAssetStore.deleteFromStore(datalandAssetId)
        return returnValue
    }

    override fun storeReceivedAsset(trusteeAssetId: String, data: ByteArray): Response {
        if (!euroDaTAssetCache.isAssetExpected(trusteeAssetId)) {
            context.monitor.info("Received asset POST request by EuroDaT for UNEXPECTED asset ID $trusteeAssetId.")
            return Response.status(Response.Status.FORBIDDEN).build()
        }

        context.monitor.info("Received asset POST request by EuroDaT with ID $trusteeAssetId.")
        val decodedData: Map<String, String> = objectMapper.readValue(data.decodeToString())

        euroDaTAssetCache.insertIntoCache(trusteeAssetId, decodedData["content"]!!)
        return Response.ok("Dataland-connector received asset with asset ID $trusteeAssetId").build()
    }
}
