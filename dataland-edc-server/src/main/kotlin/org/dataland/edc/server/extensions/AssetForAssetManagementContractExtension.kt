package org.dataland.edc.server.extensions

import org.dataland.edc.server.service.EurodatService
import org.eclipse.dataspaceconnector.spi.monitor.Monitor
import org.eclipse.dataspaceconnector.spi.system.Inject
import org.eclipse.dataspaceconnector.spi.system.ServiceExtension
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
    private lateinit var eurodatService: EurodatService

    @Inject
    private lateinit var monitor: Monitor

    override fun start() {
        monitor.info("Starting negotiation for the ASSET-FOR-ASSET-MANAGEMENT Meta Asset")
        assetForAssetManagementNegotiation = eurodatService.negotiateAssetForAssetManagementContract()
    }
}
