package org.dataland.edc.server.controller

import org.dataland.edc.server.api.DatalandInternalEdcApi
import org.dataland.edc.server.models.CheckHealthResponse
import org.dataland.edc.server.models.InsertDataResponse
import org.dataland.edc.server.service.DataManager
import org.eclipse.dataspaceconnector.spi.system.ServiceExtensionContext

/**
 * Implementation of the Dataland EDC Api
 * @param dataManager the in memory data manager orchestrating the required tasks
 * @param context the context containing constants and the monitor for logging
 */
class DatalandInternalEdcController(
    private val dataManager: DataManager,
    private val context: ServiceExtensionContext
) : DatalandInternalEdcApi {

    override fun checkHealth(): CheckHealthResponse {
        context.monitor.info("Received a health request.")
        return CheckHealthResponse("I am alive!")
    }

    override fun insertData(data: String): InsertDataResponse {
        context.monitor.info("Received data to store in the trustee.")
        return InsertDataResponse(dataManager.provideAssetToTrustee(data))
    }

    override fun selectDataById(dataId: String): String {
        context.monitor.info("Asset with data ID $dataId is requested.")
        return dataManager.getDataById(dataId)
    }
}
