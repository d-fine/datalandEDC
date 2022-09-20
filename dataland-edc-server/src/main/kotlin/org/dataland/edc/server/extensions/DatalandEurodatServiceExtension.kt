package org.dataland.edc.server.extensions

import org.dataland.edc.server.service.EurodatService
import org.eclipse.dataspaceconnector.spi.contract.negotiation.ConsumerContractNegotiationManager
import org.eclipse.dataspaceconnector.spi.contract.negotiation.store.ContractNegotiationStore
import org.eclipse.dataspaceconnector.spi.monitor.Monitor
import org.eclipse.dataspaceconnector.spi.system.Inject
import org.eclipse.dataspaceconnector.spi.system.Provider
import org.eclipse.dataspaceconnector.spi.system.ServiceExtension
import org.eclipse.dataspaceconnector.spi.system.ServiceExtensionContext
import org.eclipse.dataspaceconnector.spi.transfer.TransferProcessManager
import org.eclipse.dataspaceconnector.spi.transfer.store.TransferProcessStore

/**
 * Creates a EuroDaTService Instance with the required injects
 * and makes it available to other components
 */
class DatalandEurodatServiceExtension : ServiceExtension {

    @Inject
    private lateinit var transferProcessManager: TransferProcessManager

    @Inject
    private lateinit var contractNegotiationStore: ContractNegotiationStore

    @Inject
    private lateinit var consumerContractNegotiationManager: ConsumerContractNegotiationManager

    @Inject
    private lateinit var transferProcessStore: TransferProcessStore

    @Inject
    private lateinit var monitor: Monitor

    /**
     * Creates a EuroDaTService Instance with the required injects
     * and makes it available to other components
     */
    @Provider
    fun provideEurodatController(context: ServiceExtensionContext): EurodatService {
        return EurodatService(
            transferProcessManager,
            contractNegotiationStore,
            transferProcessStore,
            consumerContractNegotiationManager,
            monitor,
            context
        )
    }
}
