package org.dataland.edc.server.service

import org.dataland.edc.server.exceptions.EurodatTimeoutException
import org.dataland.edc.server.models.AssetProvisionContainer
import org.dataland.edc.server.models.EurodatAssetLocation
import org.dataland.edc.server.utils.AwaitUtils
import org.dataland.edc.server.utils.ConcurrencyUtils.getAcquiredSemaphore
import org.dataland.edc.server.utils.Constants
import org.eclipse.dataspaceconnector.spi.monitor.Monitor
import org.eclipse.dataspaceconnector.spi.system.ServiceExtensionContext
import java.util.UUID
import java.util.concurrent.TimeUnit

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
    fun provideAssetToTrustee(data: String, correlationId: String): EurodatAssetLocation {
        val assetProvisionContainer = AssetProvisionContainer(data, null, getAcquiredSemaphore(), correlationId)
        val datalandAssetId = storeAssetLocally(assetProvisionContainer)
        eurodatService.registerAssetEurodat(datalandAssetId, getLocalAssetAccessUrl(datalandAssetId), correlationId)
        monitor.info(
            "Waiting for semaphore to be released after Asset with ID $datalandAssetId is picked up by EuroDaT. " +
                "Correlation ID : $correlationId"
        )
        try {
            if (assetProvisionContainer.semaphore.tryAcquire(Constants.TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
                monitor.info("Acquired semaphore for correlation ID '$correlationId'.")
                return getEurodatAssetLocation(assetProvisionContainer, datalandAssetId, correlationId)
            } else { throw EurodatTimeoutException("Timeout error waiting for semamphore. Correlation ID: $correlationId") }
        } catch (ignore_e: Exception) {
            monitor.severe(
                "Error receiving eurodat  asset location with dataland asset ID $datalandAssetId. " +
                    "Correlation ID: '$correlationId' caused by ${ignore_e.message} StackTrace: ${ignore_e.stackTrace}"
            )
            throw ignore_e
        }
    }

    private fun getEurodatAssetLocation(
        assetProvisionContainer: AssetProvisionContainer,
        datalandAssetId: String,
        correlationId: String
    ): EurodatAssetLocation {
        val location = assetProvisionContainer.eurodatAssetLocation!!
        monitor.info(
            "Asset $datalandAssetId is stored in EuroDaT under $location . Correlation ID: '$correlationId'"
        )
        return location
    }

    /**
     * Retrieves an Asset from EuroDaT
     * @param dataLocation The location of the data in EuroDaT
     */
    fun retrieveAssetFromTrustee(dataLocation: EurodatAssetLocation, correlationId: String): String {
        monitor.info("Retrieve data with Correlation ID: $correlationId")
        val contractAgreement = eurodatService.negotiateReadContract(dataLocation, correlationId)
        eurodatAssetCache.expectAsset(dataLocation.eurodatAssetId)
        eurodatService.requestData(
            eurodatAssetId = dataLocation.eurodatAssetId,
            retrievalContractId = contractAgreement.id,
            targetURL = getLocalAssetAccessUrl(dataLocation.eurodatAssetId),
            correlationId
        )
        return AwaitUtils.awaitAssetArrival(eurodatAssetCache, dataLocation.eurodatAssetId)
    }

    private fun getLocalAssetAccessUrl(datalandAssetId: String): String =
        "$baseAddressDatalandToEurodatAssetUrl/$datalandAssetId"

    private fun storeAssetLocally(assetProvisionContainer: AssetProvisionContainer): String {
        val datalandAssetId = UUID.randomUUID().toString()
        localAssetStore.insertDataIntoStore(datalandAssetId, assetProvisionContainer)
        monitor.info(
            "Stored new local asset under ID $datalandAssetId with correlation ID: " +
                "'${assetProvisionContainer.correlationId}'"
        )
        return datalandAssetId
    }
}
