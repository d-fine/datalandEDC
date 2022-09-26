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

    override fun insertData(data: String, correlationId: String): InsertDataResponse {
        val dataId: String
        try {
            monitor.info("Received data to store in the trustee. Correlation ID: $correlationId")
            val eurodatAssetLocation = dataManager.provideAssetToTrustee(data, correlationId)
            dataId = "${eurodatAssetLocation.contractOfferId}_${eurodatAssetLocation.eurodatAssetId}"
            monitor.info("Data with ID $dataId stored in trustee. Correlation ID: $correlationId")
        } catch (ignore_e: Error) {
            monitor.severe("Error inserting Data with Correlation ID: $correlationId Errormessage: ${ignore_e.message}")
            throw ignore_e
        }
        return InsertDataResponse(dataId)
    }

    override fun selectDataById(dataId: String, correlationId: String): String {
        monitor.info("Asset with data ID $dataId is requested. Correlation ID: $correlationId")
        try {
            val splitDataId = dataId.split("_")
            if (splitDataId.size != 2) throw IllegalArgumentException("The data ID $dataId has an invalid format. Correlation ID: $correlationId")
            val eurodatAssetLocation = EurodatAssetLocation(splitDataId[0], splitDataId[1])
            val cacheResponse = eurodatAssetCache.retrieveFromCache(eurodatAssetLocation.eurodatAssetId)
            val response = cacheResponse ?: dataManager.retrieveAssetFromTrustee(eurodatAssetLocation, correlationId)
            monitor.info("Data with ID $dataId retrieved internally - Returning Data via REST. Correlation ID: $correlationId")
            return response
        } catch (ignore_e: Exception) {
            monitor.severe(
                "Error getting Asset with data ID $dataId from EuroDat. Correlation ID: $correlationId" +
                    "Errormessage: ${ignore_e.message}"
            )
            throw ignore_e
        }
    }
}
