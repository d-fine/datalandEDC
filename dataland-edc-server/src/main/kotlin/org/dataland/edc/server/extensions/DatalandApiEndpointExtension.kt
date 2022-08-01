package org.dataland.edc.server.extensions

import org.dataland.edc.server.controller.DatalandEurodatController
import org.dataland.edc.server.controller.DatalandInternalEdcController
import org.dataland.edc.server.service.DataManager
import org.eclipse.dataspaceconnector.spi.WebService
import org.eclipse.dataspaceconnector.spi.contract.negotiation.ConsumerContractNegotiationManager
import org.eclipse.dataspaceconnector.spi.contract.negotiation.store.ContractNegotiationStore
import org.eclipse.dataspaceconnector.spi.contract.offer.store.ContractDefinitionStore
import org.eclipse.dataspaceconnector.spi.message.RemoteMessageDispatcherRegistry
import org.eclipse.dataspaceconnector.spi.system.Inject
import org.eclipse.dataspaceconnector.spi.system.ServiceExtension
import org.eclipse.dataspaceconnector.spi.system.ServiceExtensionContext
import org.eclipse.dataspaceconnector.spi.transfer.TransferProcessManager
import org.eclipse.dataspaceconnector.spi.transfer.store.TransferProcessStore

class DatalandApiEndpointExtension : ServiceExtension  {

    @Inject
    private lateinit var transferProcessManager: TransferProcessManager

    @Inject
    private lateinit var contractNegotiationStore: ContractNegotiationStore

    @Inject
    private lateinit var consumerContractNegotiationManager: ConsumerContractNegotiationManager

    @Inject
    private lateinit var transferProcessStore : TransferProcessStore

    @Inject
    private lateinit var dispatcher: RemoteMessageDispatcherRegistry

    @Inject
    private lateinit var webService: WebService

    override fun name(): String {
        return "API Endpoint"
    }

    override fun initialize(context: ServiceExtensionContext) {

        val dataManager = DataManager(
            transferProcessManager,
            contractNegotiationStore,
            transferProcessStore,
            consumerContractNegotiationManager,
            context,
            dispatcher
        )

        webService.registerResource(DatalandInternalEdcController(dataManager, context))
        webService.registerResource(DatalandEurodatController(dataManager, context))
    }
}