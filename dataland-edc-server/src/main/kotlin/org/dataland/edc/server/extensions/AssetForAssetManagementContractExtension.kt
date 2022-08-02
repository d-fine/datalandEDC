package org.dataland.edc.server.extensions

import org.dataland.edc.server.service.EuroDaTService
import org.eclipse.dataspaceconnector.spi.system.Inject
import org.eclipse.dataspaceconnector.spi.system.ServiceExtension
import org.eclipse.dataspaceconnector.spi.system.ServiceExtensionContext
import org.eclipse.dataspaceconnector.spi.types.domain.contract.negotiation.ContractNegotiation

/**
 * Extension class that negotiates a contract for the
 * ASSET-FOR-ASSET-MANAGEMENT on boot
 */
class AssetForAssetManagementContractExtension : ServiceExtension {

    companion object {
        var assetForAssetManagementNegotiation: ContractNegotiation? = null
    }

    @Inject
    private lateinit var euroDatService: EuroDaTService

    private lateinit var context: ServiceExtensionContext

    override fun initialize(context: ServiceExtensionContext) {
        this.context = context
    }

    override fun start() {
        context.monitor.info("Starting negotiation for the ASSET-FOR-ASSET-MANAGEMENT Meta Asset")
        assetForAssetManagementNegotiation = euroDatService.negotiateAssetForAssetManagementContract()
    }
}
