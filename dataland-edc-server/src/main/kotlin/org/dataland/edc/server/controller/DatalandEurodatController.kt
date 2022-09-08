package org.dataland.edc.server.controller

import jakarta.ws.rs.core.Response
import org.dataland.edc.server.api.DatalandEurodatApi
import org.dataland.edc.server.models.EurodatAssetLocation
import org.dataland.edc.server.service.EurodatAssetCache
import org.dataland.edc.server.service.LocalAssetStore
import org.dataland.edc.server.service.ThreadAwareMonitor
import org.dataland.edc.server.utils.Constants
import org.eclipse.dataspaceconnector.spi.system.ServiceExtensionContext

/**
 * Implementation of the Dataland EDC Api
 * @param context the context containing constants and the monitor for logging
 * @param localAssetStore a store of the assets provided to EuroDaT
 * @param eurodatAssetCache a cache of assets received by EuroDaT
 * @param threadAwareMonitor a monitor that also exposes thread information
 */
class DatalandEurodatController(
    private val context: ServiceExtensionContext,
    private val localAssetStore: LocalAssetStore,
    private val eurodatAssetCache: EurodatAssetCache,
    private val threadAwareMonitor: ThreadAwareMonitor
) : DatalandEurodatApi {

    override fun provideAsset(
        datalandAssetId: String,
        eurodatAssetId: String,
        eurodatContractDefinitionId: String
    ): String {
        threadAwareMonitor.info("EuroDat retrieves asset with dataland asset ID $datalandAssetId.")
        threadAwareMonitor.info("EuroDat Asset ID is given by $eurodatAssetId.")
        threadAwareMonitor.info("EuroDat Contract ID is given by $eurodatContractDefinitionId.")
        localAssetStore.insertEurodatAssetLocationIntoStore(
            datalandAssetId,
            EurodatAssetLocation(
                contractOfferId = "$eurodatContractDefinitionId:${Constants.DUMMY_STRING}",
                eurodatAssetId = eurodatAssetId
            )
        )
        return localAssetStore.retrieveDataFromStore(datalandAssetId) ?: ""
    }

    override fun storeReceivedAsset(eurodatAssetId: String, data: ByteArray): Response {
        if (!eurodatAssetCache.isAssetExpected(eurodatAssetId)) {
            threadAwareMonitor.info("Received asset POST request for an UNEXPECTED asset ID $eurodatAssetId.")
            return Response.status(Response.Status.BAD_REQUEST).build()
        }

        threadAwareMonitor.info("Received asset POST request by EuroDaT with ID $eurodatAssetId.")
        eurodatAssetCache.insertIntoCache(eurodatAssetId, data.decodeToString())
        return Response.ok("Dataland-connector received asset with asset ID $eurodatAssetId").build()
    }
}
