package org.dataland.edc.server.extensions

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.dataland.edc.server.utils.Constants
import org.eclipse.dataspaceconnector.policy.model.Action
import org.eclipse.dataspaceconnector.policy.model.Permission
import org.eclipse.dataspaceconnector.policy.model.Policy
import org.eclipse.dataspaceconnector.spi.contract.negotiation.ConsumerContractNegotiationManager
import org.eclipse.dataspaceconnector.spi.contract.negotiation.ProviderContractNegotiationManager
import org.eclipse.dataspaceconnector.spi.contract.negotiation.store.ContractNegotiationStore
import org.eclipse.dataspaceconnector.spi.system.Inject
import org.eclipse.dataspaceconnector.spi.system.ServiceExtension
import org.eclipse.dataspaceconnector.spi.system.ServiceExtensionContext
import org.eclipse.dataspaceconnector.spi.types.domain.asset.Asset
import org.eclipse.dataspaceconnector.spi.types.domain.contract.negotiation.ContractNegotiation
import org.eclipse.dataspaceconnector.spi.types.domain.contract.negotiation.ContractOfferRequest
import org.eclipse.dataspaceconnector.spi.types.domain.contract.offer.ContractOffer
import java.net.URI
import java.util.*

class AssetForAssetManagementContractExtension : ServiceExtension {

    companion object {
        var assetForAssetManagementNegotiation : ContractNegotiation? = null;
    }

    @Inject
    private lateinit var contractNegotiationStore: ContractNegotiationStore

    @Inject
    private lateinit var consumerContractNegotiationManager: ConsumerContractNegotiationManager

    override fun start() {
        super.start()

        val action = Action.Builder.newInstance()
            .type(Constants.ACTION_TYPE_USE)
            .build()

        val assetPermission = Permission.Builder.newInstance()
            .target(Constants.ASSET_ID_ASSET_FOR_ASSET_MANAGEMENT)
            .action(action)
            .build()

        val assetPolicy = Policy.Builder.newInstance()
            .target(Constants.ASSET_ID_ASSET_FOR_ASSET_MANAGEMENT)
            .permission(assetPermission)
            .build()

        val asset = Asset.Builder.newInstance()
            .id(Constants.ASSET_ID_ASSET_FOR_ASSET_MANAGEMENT)
            .build()

        val assetContractOffer = ContractOffer.Builder.newInstance()
            .id("contract-def-id:${UUID.randomUUID()}")
            .asset(asset)
            .policy(assetPolicy)
            .provider(URI(Constants.URN_KEY_PROVIDER))
            .consumer(URI(Constants.URN_KEY_CONSUMER))
            .build()

        val contractOfferRequest = ContractOfferRequest.Builder.newInstance()
            .type(ContractOfferRequest.Type.INITIAL)
            .connectorId(Constants.CONNECTOR_ID_PROVIDER)
            .connectorAddress(Constants.CONNECTOR_ADDRESS_EURODAT)
            .protocol("ids-multipart")
            .contractOffer(assetContractOffer)
            .build()

        val oj = jacksonObjectMapper()
        println(oj.writeValueAsString(contractOfferRequest))

        assetForAssetManagementNegotiation = consumerContractNegotiationManager.initiate(contractOfferRequest).content
    }
}