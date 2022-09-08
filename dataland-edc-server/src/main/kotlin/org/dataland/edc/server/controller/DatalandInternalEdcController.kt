package org.dataland.edc.server.controller

import org.dataland.edc.server.api.DatalandInternalEdcApi
import org.dataland.edc.server.models.CheckHealthResponse
import org.dataland.edc.server.models.EurodatAssetLocation
import org.dataland.edc.server.models.InsertDataResponse
import org.dataland.edc.server.service.DataManager
import org.dataland.edc.server.service.EurodatAssetCache
import org.dataland.edc.server.service.ThreadAwareMonitor
import org.eclipse.dataspaceconnector.spi.system.ServiceExtensionContext

/**
 * Implementation of the Dataland EDC Api
 * @param dataManager the in memory data manager orchestrating the required tasks
 * @param context the context containing constants and the monitor for logging
 * @param eurodatAssetCache a cache for files that already were received by EuroDaT
 * @param threadAwareMonitor a monitor that also exposes thread information
 */
class DatalandInternalEdcController(
    private val dataManager: DataManager,
    private val context: ServiceExtensionContext,
    private val eurodatAssetCache: EurodatAssetCache,
    private val threadAwareMonitor: ThreadAwareMonitor
) : DatalandInternalEdcApi {

    override fun checkHealth(): CheckHealthResponse {
        threadAwareMonitor.info("Received a health request.")
        return CheckHealthResponse("I am alive!")
    }

    override fun insertData(data: String): InsertDataResponse {
        threadAwareMonitor.info("Received data to store in the trustee.")
        val eurodatAssetLocation = dataManager.provideAssetToTrustee(data)
        val dataId = "${eurodatAssetLocation.contractOfferId}_${eurodatAssetLocation.eurodatAssetId}"
        threadAwareMonitor.info("Data with ID $dataId stored in trustee")
        return InsertDataResponse(dataId)
    }

    override fun selectDataById(dataId: String): String {
        threadAwareMonitor.info("Asset with data ID $dataId is requested.")
        val splitDataId = dataId.split("_")
        if (splitDataId.size != 2) throw IllegalArgumentException("The data ID $dataId has an invalid format.")
        val eurodatAssetLocation = EurodatAssetLocation(
            contractOfferId = splitDataId[0],
            eurodatAssetId = splitDataId[1]
        )

        val cacheResponse = eurodatAssetCache.retrieveFromCache(eurodatAssetLocation.eurodatAssetId)
        val response = cacheResponse ?: dataManager.retrieveAssetFromTrustee(eurodatAssetLocation)
        threadAwareMonitor.info("Data with ID $dataId retrieved internally - Returning Data via REST")
        return response
    }
}
