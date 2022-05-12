package org.dataland.edc.server.api

import org.dataland.edc.server.controller.DatalandEurodatController
import org.dataland.edc.server.controller.DatalandInternalEdcController
import org.dataland.edc.server.service.DataManager
import org.eclipse.dataspaceconnector.dataloading.AssetLoader
import org.eclipse.dataspaceconnector.spi.WebService
import org.eclipse.dataspaceconnector.spi.contract.negotiation.ConsumerContractNegotiationManager
import org.eclipse.dataspaceconnector.spi.contract.negotiation.store.ContractNegotiationStore
import org.eclipse.dataspaceconnector.spi.contract.offer.store.ContractDefinitionStore
import org.eclipse.dataspaceconnector.spi.system.Inject
import org.eclipse.dataspaceconnector.spi.system.Requires
import org.eclipse.dataspaceconnector.spi.system.ServiceExtension
import org.eclipse.dataspaceconnector.spi.system.ServiceExtensionContext
import org.eclipse.dataspaceconnector.spi.transfer.TransferProcessManager

/**
 * Extends the EDC with the functionality required for Dataland
 */
@Requires(WebService::class)
class DatalandApiEndpointExtension : ServiceExtension {

    @Inject
    val assetLoader: AssetLoader? = null

    @Inject
    val contractDefinitionStore: ContractDefinitionStore? = null

    @Inject
    private val transferProcessManager: TransferProcessManager? = null

    @Inject
    private val contractNegotiationStore: ContractNegotiationStore? = null

    @Inject
    private val consumerContractNegotiationManager: ConsumerContractNegotiationManager? = null

    @Inject
    val webService: WebService? = null

    override fun name(): String {
        return "API Endpoint"
    }

    override fun initialize(context: ServiceExtensionContext) {
        val dataManager = DataManager(
            assetLoader!!,
            contractDefinitionStore!!,
            transferProcessManager!!,
            contractNegotiationStore!!,
            consumerContractNegotiationManager!!,
            context
        )
        webService!!.registerResource(DatalandInternalEdcController(dataManager, context))
        webService.registerResource(DatalandEurodatController(dataManager, context))
    }
}
