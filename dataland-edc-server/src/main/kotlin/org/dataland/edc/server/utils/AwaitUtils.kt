package org.dataland.edc.server.utils

import org.awaitility.Awaitility.await
import org.awaitility.core.ConditionTimeoutException
import org.dataland.edc.server.service.EurodatAssetCache
import org.eclipse.dataspaceconnector.spi.contract.negotiation.store.ContractNegotiationStore
import org.eclipse.dataspaceconnector.spi.monitor.Monitor
import org.eclipse.dataspaceconnector.spi.transfer.store.TransferProcessStore
import org.eclipse.dataspaceconnector.spi.types.domain.contract.agreement.ContractAgreement
import org.eclipse.dataspaceconnector.spi.types.domain.contract.negotiation.ContractNegotiation
import org.eclipse.dataspaceconnector.spi.types.domain.contract.negotiation.ContractNegotiationStates
import org.eclipse.dataspaceconnector.spi.types.domain.transfer.TransferProcessStates
import java.time.Duration

/**
 * An object for various busy-waiting activities
 */
object AwaitUtils {

    /**
     * Waits for the confirmation of a contract
     * @param contractNegotiationStore the contract store
     * @param contractNegotiation the negotiation to wait for
     */
    fun awaitContractConfirm(
        contractNegotiationStore: ContractNegotiationStore,
        contractNegotiation: ContractNegotiation
    ): ContractAgreement {
        metaAwait({
            val negotiation = contractNegotiationStore.find(contractNegotiation.id)!!.state
            ContractNegotiationStates.from(negotiation) == ContractNegotiationStates.CONFIRMED
        })
        return contractNegotiationStore.find(contractNegotiation.id)!!.contractAgreement
    }

    /**
     * Waits for a transfer to complete
     * @param transferProcessStore the transfer store
     * @param transferId the id of the transfer to wait for
     */
    fun awaitTransferCompletion(transferProcessStore: TransferProcessStore, transferId: String) {
        metaAwait({
            TransferProcessStates.from(transferProcessStore.find(transferId)!!.state) == TransferProcessStates.COMPLETED
        })
    }

    /**
     * Awaits the arrival of an asset in the asset cache
     * @param cache the asset cache
     * @param id the id of the asset to wait for
     */
    fun awaitAssetArrival(cache: EurodatAssetCache, id: String): String {
        metaAwait({
            cache.retrieveFromCache(id) != null
        })
        return cache.retrieveFromCache(id)!!
    }

    private fun metaAwait(
        condition: () -> Boolean,
        monitor: Monitor? = null,
        waitingForInformation: String = "",
        exceptionProvider: (Exception) -> Exception = { it }
    ) {
        try {
            monitor?.info("Waiting for $waitingForInformation")
            await()
                .atMost(Duration.ofMillis(Constants.TIMEOUT_MS))
                .pollInterval(Duration.ofMillis(Constants.POLL_INTERVAL_MS))
                .until(condition)
        } catch (e: ConditionTimeoutException) {
            monitor?.severe("Timeout waiting for $waitingForInformation")
            throw exceptionProvider(e)
        }
    }
}
