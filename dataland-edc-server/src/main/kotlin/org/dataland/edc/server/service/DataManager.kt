package org.dataland.edc.server.service

import org.dataland.edc.server.models.EuroDaTAssetLocation
import org.dataland.edc.server.utils.AwaitUtils
import org.eclipse.dataspaceconnector.spi.system.ServiceExtensionContext
import java.util.UUID

/**
 * Entity orchestrating the required steps for trustee data exchange
 * @param context the context containing constants and the monitor for logging
 * @param euroDaTService The euroDaT service responsible for communicating with EuroDat
 * @param localAssetStore The storage for local assets that are made available to EuroDat
 * @param euroDatCache A cache holding retrieved EuroDaT assets
 */
class DataManager(
    private val context: ServiceExtensionContext,
    private val euroDaTService: EuroDaTService,
    private val localAssetStore: LocalAssetStore,
    private val euroDatCache: EuroDaTAssetCache
) {

    private val baseAddressDatalandToEuroDatAssetUrl: String = context.getSetting("dataland.edc.web.uri", "default")

    /**
     * Registers a new asset with EuroDaT and returns
     * identifying information returned for the newly created asset
     * @param data The data to store in EuroDaT
     */
    fun provideAssetToTrustee(data: String): EuroDaTAssetLocation {
        val localAssetId = registerAssetLocally(data)
        euroDaTService.registerAssetEuroDat(localAssetId, getLocalAssetAccessURl(localAssetId))
        return euroDaTService.getAssetFromEuroDatCatalog(localAssetId)
    }

    /**
     * Retrieves an Asset from EuroDaT
     * @param dataLocation The location of the data in EuroDaT
     */
    fun retrieveAssetFromTrustee(dataLocation: EuroDaTAssetLocation): String {
        val agreement = euroDaTService.negotiateReadContract(dataLocation)
        euroDatCache.expectAsset(dataLocation.assetId)
        euroDaTService.requestData(
            euroDatAssetId = dataLocation.assetId,
            retrievalContractId = agreement.id,
            targetURL = getLocalAssetAccessURl(dataLocation.assetId))
        return AwaitUtils.awaitAssetArrival(euroDatCache, dataLocation.assetId)
    }

    private fun getLocalAssetAccessURl(localAssetID: String): String =
        "$baseAddressDatalandToEuroDatAssetUrl/$localAssetID"

    private fun registerAssetLocally(data: String): String {
        val datalandAssetId = UUID.randomUUID().toString()
        context.monitor.info("Registered new local asset under ID $datalandAssetId)")
        localAssetStore.insertIntoStore(datalandAssetId, data)
        return datalandAssetId
    }
}
