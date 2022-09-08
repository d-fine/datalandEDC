package org.dataland.edc.server.controller

import org.dataland.edc.server.api.DatalandInternalEdcApi
import org.dataland.edc.server.models.CheckHealthResponse
import org.dataland.edc.server.models.EurodatAssetLocation
import org.dataland.edc.server.models.InsertDataResponse
import org.dataland.edc.server.service.DataManager
import org.dataland.edc.server.service.EurodatAssetCache
import org.eclipse.dataspaceconnector.spi.monitor.Monitor

/**
 * Implementation of the Dataland EDC Api
 * @param dataManager the in memory data manager orchestrating the required tasks
 * @param eurodatAssetCache a cache for files that already were received by EuroDaT
 * @param monitor a monitor that also exposes thread information
 */
class DatalandInternalEdcController(
    private val dataManager: DataManager,
    private val eurodatAssetCache: EurodatAssetCache,
    private val monitor: Monitor
) : DatalandInternalEdcApi {

    override fun checkHealth(): CheckHealthResponse {
        monitor.info("Received a health request.")
        return CheckHealthResponse("I am alive!")
    }

    override fun insertData(data: String): InsertDataResponse {
        val dataId: String
        try {
            monitor.info("Received data to store in the trustee.")
            val eurodatAssetLocation = dataManager.provideAssetToTrustee(data)
            dataId = "${eurodatAssetLocation.contractOfferId}_${eurodatAssetLocation.eurodatAssetId}"
            monitor.info("Data with ID $dataId stored in trustee")
        } catch (ignore_e: Error) {
            monitor.info("Error inserting Data. Errormessage: ${ignore_e.message}")
            throw ignore_e
        }
        return InsertDataResponse(dataId)
    }

    override fun selectDataById(dataId: String): String {
        monitor.info("Asset with data ID $dataId is requested.")
        val response: String
        try {
            val splitDataId = dataId.split("_")
            if (splitDataId.size != 2) throw IllegalArgumentException("The data ID $dataId has an invalid format.")
            val eurodatAssetLocation = EurodatAssetLocation(
                contractOfferId = splitDataId[0],
                eurodatAssetId = splitDataId[1]
            )

            val cacheResponse = eurodatAssetCache.retrieveFromCache(eurodatAssetLocation.eurodatAssetId)
            response = cacheResponse ?: dataManager.retrieveAssetFromTrustee(eurodatAssetLocation)
            monitor.info("Data with ID $dataId retrieved internally - Returning Data via REST")
        } catch (ignore_e: Exception) {
            monitor.info(
                "Error getting Asset with data ID $dataId from EuroDat." +
                    "Errormessage: ${ignore_e.message}"
            )
            throw ignore_e
        }
        return response
    }
}
