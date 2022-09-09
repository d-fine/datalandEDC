package org.dataland.edc.server.service

import org.dataland.edc.server.models.AssetProvisionContainer
import org.dataland.edc.server.models.EurodatAssetLocation
import org.dataland.edc.server.utils.AwaitUtils
import org.eclipse.dataspaceconnector.spi.monitor.Monitor
import org.eclipse.dataspaceconnector.spi.system.ServiceExtensionContext
import java.util.UUID
import java.util.concurrent.locks.ReentrantLock

/**
 * Entity orchestrating the required steps for trustee data exchange
 * @param context the context containing constants and the monitor for logging
 * @param eurodatService The euroDaT service responsible for communicating with EuroDat
 * @param localAssetStore The storage for local assets that are made available to EuroDat
 * @param eurodatAssetCache A cache holding retrieved EuroDaT assets
 * @param monitor a monitor that also exposes thread information
 */
class DataManager(
    private val context: ServiceExtensionContext,
    private val eurodatService: EurodatService,
    private val localAssetStore: LocalAssetStore,
    private val eurodatAssetCache: EurodatAssetCache,
    private val monitor: Monitor,
) {

    private val baseAddressDatalandToEurodatAssetUrl: String = context.getSetting(
        "dataland.edc.asset.access.uri",
        "default"
    )

    /**
     * Registers a new asset with EuroDaT and returns
     * identifying information returned for the newly created asset
     * @param data The data to store in EuroDaT
     */
    fun provideAssetToTrustee(data: String): EurodatAssetLocation {
        val lock = ReentrantLock()
        lock.lock()
        val assetProvisionContainer = AssetProvisionContainer(data, null, lock)
        val datalandAssetId = storeAssetLocally(assetProvisionContainer)
        eurodatService.registerAssetEurodat(datalandAssetId, getLocalAssetAccessUrl(datalandAssetId))
        try {
            monitor.info("Trying to get a lock after provision of Asset with ID $datalandAssetId to EuroDaT")
            lock.lock()
            val location = assetProvisionContainer.eurodatAssetLocation!!
            monitor.info("Asset $datalandAssetId is stored in EuroDaT under $location")
            return location
        } finally {
            localAssetStore.deleteFromStore(datalandAssetId)
        }
    }

    /**
     * Retrieves an Asset from EuroDaT
     * @param dataLocation The location of the data in EuroDaT
     */
    fun retrieveAssetFromTrustee(dataLocation: EurodatAssetLocation): String {
        val contractAgreement = eurodatService.negotiateReadContract(dataLocation)
        eurodatAssetCache.expectAsset(dataLocation.eurodatAssetId)
        eurodatService.requestData(
            eurodatAssetId = dataLocation.eurodatAssetId,
            retrievalContractId = contractAgreement.id,
            targetURL = getLocalAssetAccessUrl(dataLocation.eurodatAssetId)
        )
        return AwaitUtils.awaitAssetArrival(eurodatAssetCache, dataLocation.eurodatAssetId)
    }

    private fun getLocalAssetAccessUrl(datalandAssetId: String): String =
        "$baseAddressDatalandToEurodatAssetUrl/$datalandAssetId"

    private fun storeAssetLocally(assetProvisionContainer: AssetProvisionContainer): String {
        val datalandAssetId = UUID.randomUUID().toString()
        localAssetStore.insertDataIntoStore(datalandAssetId, assetProvisionContainer)
        monitor.info("Stored new local asset under ID $datalandAssetId)")
        return datalandAssetId
    }
}
