package org.dataland.edc.server.controller

import jakarta.ws.rs.core.Response
import org.dataland.edc.server.api.DatalandEurodatApi
import org.dataland.edc.server.models.EurodatAssetLocation
import org.dataland.edc.server.service.EurodatAssetCache
import org.dataland.edc.server.service.LocalAssetStore
import org.dataland.edc.server.utils.Constants
import org.eclipse.dataspaceconnector.spi.monitor.Monitor

/**
 * Implementation of the Dataland EDC Api
 * @param localAssetStore a store of the assets provided to EuroDaT
 * @param eurodatAssetCache a cache of assets received by EuroDaT
 * @param monitor a monitor that also exposes thread information
 */
class DatalandEurodatController(
    private val localAssetStore: LocalAssetStore,
    private val eurodatAssetCache: EurodatAssetCache,
    private val monitor: Monitor
) : DatalandEurodatApi {

    override fun provideAsset(
        datalandAssetId: String,
        eurodatAssetId: String,
        eurodatContractDefinitionId: String
    ): String {
        monitor.info("EuroDat retrieves asset with dataland asset ID $datalandAssetId.")
        monitor.info("EuroDat Asset ID is given by $eurodatAssetId.")
        monitor.info("EuroDat Contract ID is given by $eurodatContractDefinitionId.")
        try {
            localAssetStore.insertEurodatAssetLocationIntoStore(
                datalandAssetId,
                EurodatAssetLocation("$eurodatContractDefinitionId:${Constants.DUMMY_STRING}", eurodatAssetId)
            )
            return localAssetStore.retrieveDataFromStore(datalandAssetId) ?: ""
        } catch (ignore_e: Exception) {
            monitor.severe(
                "Error providing an Asset with dataland asset ID $datalandAssetId, EuroDat Asset ID " +
                    "$eurodatAssetId, Contract ID $eurodatContractDefinitionId Errormessage: ${ignore_e.message}"
            )
            throw ignore_e
        }
    }

    override fun storeReceivedAsset(eurodatAssetId: String, data: ByteArray): Response {
        try {
            if (!eurodatAssetCache.isAssetExpected(eurodatAssetId)) {
                monitor.info("Received asset POST request for an UNEXPECTED asset ID $eurodatAssetId.")
                return Response.status(Response.Status.BAD_REQUEST).build()
            }

            monitor.info("Received asset POST request by EuroDaT with ID $eurodatAssetId.")
            eurodatAssetCache.insertIntoCache(eurodatAssetId, data.decodeToString())
        } catch (ignore_e: Exception) {
            monitor.severe("Error receiving Asset with asset ID $eurodatAssetId. Errormessage: ${ignore_e.message}")
            throw ignore_e
        }
        return Response.ok("Dataland-connector received asset with asset ID $eurodatAssetId").build()
    }
}
