package org.dataland.edc.server.utils

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.awaitility.Awaitility.await
import org.eclipse.dataspaceconnector.spi.contract.negotiation.store.ContractNegotiationStore
import org.eclipse.dataspaceconnector.spi.transfer.store.TransferProcessStore
import org.eclipse.dataspaceconnector.spi.types.domain.contract.agreement.ContractAgreement
import org.eclipse.dataspaceconnector.spi.types.domain.contract.negotiation.ContractNegotiation
import org.eclipse.dataspaceconnector.spi.types.domain.contract.negotiation.ContractNegotiationStates
import org.eclipse.dataspaceconnector.spi.types.domain.transfer.TransferProcessStates

object AwaitUtils {

    fun awaitContractConfirm(contractNegotiationStore: ContractNegotiationStore, contractNegotiation : ContractNegotiation) : ContractAgreement {
        metaAwait {
            ContractNegotiationStates.from(contractNegotiationStore.find(contractNegotiation.id)!!.state) == ContractNegotiationStates.CONFIRMED
        }
        return contractNegotiationStore.find(contractNegotiation.id)!!.contractAgreement
    }

    fun awaitTransferCompletion(transferProcessStore: TransferProcessStore, transferId : String) {
        metaAwait {
            TransferProcessStates.from(transferProcessStore.find(transferId)!!.state) == TransferProcessStates.COMPLETED
        }
    }

    fun metaAwait(condition : () -> Boolean) {
        await()
            .atMost(Constants.TIMEOUT)
            .pollInterval(Constants.POLL_INTERVAL)
            .until(condition)
    }
}