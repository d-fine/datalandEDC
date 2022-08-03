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

    private val baseAddressDatalandToEuroDatAssetUrl: String = context.getSetting(
        "dataland.edc.asset.access.uri",
        "default"
    )

    /**
     * Registers a new asset with EuroDaT and returns
     * identifying information returned for the newly created asset
     * @param data The data to store in EuroDaT
     */
    fun provideAssetToTrustee(data: String): EuroDaTAssetLocation {
        val localAssetId = storeAssetLocally(data)
        euroDaTService.registerAssetEuroDat(localAssetId, getLocalAssetAccessURl(localAssetId))
        val location = euroDaTService.getAssetFromEuroDatCatalog(localAssetId)
        context.monitor.info("Asset $localAssetId is stored in EuroDaT under $location")
        return location
    }

    /**
     * Retrieves an Asset from EuroDaT
     * @param dataLocation The location of the data in EuroDaT
     */
    fun retrieveAssetFromTrustee(dataLocation: EuroDaTAssetLocation): String {
        val contractAgreement = euroDaTService.negotiateReadContract(dataLocation)
        euroDatCache.expectAsset(dataLocation.assetId)
        euroDaTService.requestData(
            euroDatAssetId = dataLocation.assetId,
            retrievalContractId = contractAgreement.id,
            targetURL = getLocalAssetAccessURl(dataLocation.assetId)
        )
        return AwaitUtils.awaitAssetArrival(euroDatCache, dataLocation.assetId)
    }

    private fun getLocalAssetAccessURl(localAssetID: String): String =
        "$baseAddressDatalandToEuroDatAssetUrl/$localAssetID"

    private fun storeAssetLocally(data: String): String {
        val datalandAssetId = UUID.randomUUID().toString()
        localAssetStore.insertIntoStore(datalandAssetId, data)
        context.monitor.info("Stored new local asset under ID $datalandAssetId)")
        return datalandAssetId
    }
}
