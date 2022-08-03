package org.dataland.edc.server.controller

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import jakarta.ws.rs.core.Response
import org.dataland.edc.server.api.DatalandEurodatApi
import org.dataland.edc.server.service.EurodatAssetCache
import org.dataland.edc.server.service.LocalAssetStore
import org.eclipse.dataspaceconnector.spi.system.ServiceExtensionContext

/**
 * Implementation of the Dataland EDC Api
 * @param localAssetStore a store of the assets provided to EuroDaT
 * @param eurodatAssetCache a cache of assets received by EuroDaT
 * @param context the context containing constants and the monitor for logging
 */
class DatalandEurodatController(
    private val context: ServiceExtensionContext,
    private val localAssetStore: LocalAssetStore,
    private val eurodatAssetCache: EurodatAssetCache
) : DatalandEurodatApi {

    private val objectMapper = jacksonObjectMapper()

    override fun provideAsset(datalandAssetId: String): String {
        context.monitor.info("Asset with dataland asset ID $datalandAssetId is requested.")
        val assetToProvide = localAssetStore.retrieveFromStore(datalandAssetId) ?: ""
        localAssetStore.deleteFromStore(datalandAssetId)
        return assetToProvide
    }

    override fun storeReceivedAsset(eurodatAssetId: String, data: ByteArray): Response {
        if (!eurodatAssetCache.isAssetExpected(eurodatAssetId)) {
            context.monitor.info("Received asset POST request for an UNEXPECTED asset ID $eurodatAssetId.")
            return Response.status(Response.Status.BAD_REQUEST).build()
        }

        context.monitor.info("Received asset POST request by EuroDaT with ID $eurodatAssetId.")
        val decodedData: Map<String, String> = objectMapper.readValue(data.decodeToString())

        eurodatAssetCache.insertIntoCache(eurodatAssetId, decodedData["content"]!!)
        return Response.ok("Dataland-connector received asset with asset ID $eurodatAssetId").build()
    }
}
