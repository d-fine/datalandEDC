package org.dataland.edc.server.service

import org.awaitility.core.ConditionTimeoutException
import org.dataland.edc.server.extensions.AssetForAssetManagementContractExtension
import org.dataland.edc.server.models.EuroDaTAssetLocation
import org.dataland.edc.server.utils.AwaitUtils
import org.dataland.edc.server.utils.Constants
import org.eclipse.dataspaceconnector.policy.model.Action
import org.eclipse.dataspaceconnector.policy.model.Permission
import org.eclipse.dataspaceconnector.policy.model.Policy
import org.eclipse.dataspaceconnector.spi.EdcException
import org.eclipse.dataspaceconnector.spi.contract.negotiation.ConsumerContractNegotiationManager
import org.eclipse.dataspaceconnector.spi.contract.negotiation.store.ContractNegotiationStore
import org.eclipse.dataspaceconnector.spi.message.Range
import org.eclipse.dataspaceconnector.spi.message.RemoteMessageDispatcherRegistry
import org.eclipse.dataspaceconnector.spi.system.ServiceExtensionContext
import org.eclipse.dataspaceconnector.spi.transfer.TransferProcessManager
import org.eclipse.dataspaceconnector.spi.transfer.store.TransferProcessStore
import org.eclipse.dataspaceconnector.spi.types.domain.DataAddress
import org.eclipse.dataspaceconnector.spi.types.domain.asset.Asset
import org.eclipse.dataspaceconnector.spi.types.domain.catalog.Catalog
import org.eclipse.dataspaceconnector.spi.types.domain.catalog.CatalogRequest
import org.eclipse.dataspaceconnector.spi.types.domain.contract.agreement.ContractAgreement
import org.eclipse.dataspaceconnector.spi.types.domain.contract.negotiation.ContractNegotiation
import org.eclipse.dataspaceconnector.spi.types.domain.contract.negotiation.ContractOfferRequest
import org.eclipse.dataspaceconnector.spi.types.domain.contract.offer.ContractOffer
import org.eclipse.dataspaceconnector.spi.types.domain.transfer.DataRequest
import java.net.URI
import java.util.UUID

/**
 * A service class that handles all the EDC-based communication
 * with EuroDat
 * @param transferProcessManager manages the transfer process
 * @param transferProcessStore holds information about transfers
 * @param dispatcher used to send commands to EuroDaT
 * @param contractNegotiationStore holds the contract negotiations of the Dataland EDC
 * @param consumerContractNegotiationManager manages contract negotiations
 * @param context the context containing constants and the monitor for logging
 */
class EuroDaTService(
    private val transferProcessManager: TransferProcessManager,
    private val contractNegotiationStore: ContractNegotiationStore,
    private val transferProcessStore: TransferProcessStore,
    private val consumerContractNegotiationManager: ConsumerContractNegotiationManager,
    private val context: ServiceExtensionContext,
    private val dispatcher: RemoteMessageDispatcherRegistry,
) {
    private val constantDummyDataDestination = DataAddress.Builder.newInstance()
        .type("")
        .property("endpoint", "unused-endpoint")
        .build()

    private val connectorAddressEuroDat = context.getSetting("trustee.ids.uri", "default")

    private fun buildPropertiesForAssetRegistration(endpoint: String, assetName: String): Map<String, String> {
        return mapOf(
            "type" to Constants.TYPE_HTTP_ASSET_REGISTRATION,
            "endpoint" to endpoint,
            "providerId" to Constants.PROVIDER_ID_DATALAND,
            "ownerId" to Constants.OWNER_ID_DATALAND,
            "contentType" to Constants.CONTENT_TYPE_PERSISTENT,
            "policyTemplateId" to Constants.POLICY_TEMPLATE_ID,
            "assetName" to assetName,
            "providerAssetId" to "",
            "queryAgreementId" to "",
        )
    }

    /**
     * Awaits the confirmation of the Asset-For-Asset-Management contract
     * that is negotiated at the startup of this service. If the contract does not
     * get confirmed in one timeout session
     * (because e.g. EuroDaT might not have been available at the start of this EDC)
     * A new contract for the Asset-For-Asset-Management asset is negotiated
     */
    private fun awaitAssetForAssetManagementContractConfirm(): ContractAgreement {
        return try {
            AwaitUtils.awaitContractConfirm(
                contractNegotiationStore,
                AssetForAssetManagementContractExtension.assetForAssetManagementNegotiation!!
            )
        } catch (ex: ConditionTimeoutException) {
            context.monitor.severe("Negotiation for the ASSET-FOR-ASSET-MANAGEMENT failed ($ex). Renegotiating...")
            val assetForAssetManagementContractNegotiation = negotiateAssetForAssetManagementContract()
            val contractAgreement = AwaitUtils.awaitContractConfirm(
                contractNegotiationStore,
                assetForAssetManagementContractNegotiation
            )
            AssetForAssetManagementContractExtension.assetForAssetManagementNegotiation =
                assetForAssetManagementContractNegotiation
            contractAgreement
        }
    }

    /**
     * Registers an asset with EuroDaT using the "asset-for-asset-management" Meta
     * Asset (Ref
     * https://gitlab.com/eurodat.org/trustee-platform/-/blob/88fb32f46c87e9ed3016ef340fb6c09a9bcc9d65
     * /docs/eurodat-user-tutorial/broker.md
     * The asset is registered with EuroDaT. EuroDaT then performs an HTTP Get request to the localAssetAccessURL
     * @param localAssetId the dataland asset id
     * @param localAssetAccessURL a publicly reachable URL under which EuroDaT can retrieve the asset
     */
    @Suppress("kotlin:S138")
    fun registerAssetEuroDat(localAssetId: String, localAssetAccessURL: String) {
        context.monitor.info("Registering asset $localAssetId with EuroDat")
        val assetForAssetManagementContractConfirmation = awaitAssetForAssetManagementContractConfirm()

        val dataRequest = DataRequest.Builder.newInstance()
            .id("process-id:$localAssetId")
            .protocol(Constants.PROTOCOL_IDS_MULTIPART)
            .connectorAddress(connectorAddressEuroDat)
            .connectorId(Constants.CONNECTOR_ID_PROVIDER)
            .assetId(Constants.ASSET_ID_ASSET_FOR_ASSET_MANAGEMENT)
            .contractId(assetForAssetManagementContractConfirmation.id)
            .dataDestination(constantDummyDataDestination)
            .managedResources(false)
            .properties(
                buildPropertiesForAssetRegistration(endpoint = localAssetAccessURL, assetName = localAssetId)
            )
            .build()

        val transferId = transferProcessManager.initiateConsumerRequest(dataRequest).content
        AwaitUtils.awaitTransferCompletion(transferProcessStore, transferId)
    }

    private fun retrieveEuroDatCatalog(startIndex: Int, endIndex: Int): Catalog {
        val request = CatalogRequest.Builder.newInstance()
            .protocol(Constants.PROTOCOL_IDS_MULTIPART)
            .connectorId(Constants.CONNECTOR_ID_PROVIDER)
            .connectorAddress(connectorAddressEuroDat)
            .range(Range(startIndex, endIndex))
            .build()

        val catalogFuture = dispatcher.send(Catalog::class.java, request) { null }
        val catalog = catalogFuture.join()
        return catalog
    }

    /**
     * Searches the EuroDaT asset catalog for an asset that has been registered with
     * EuroDaT under the localAssetId. This only works because the registerAssetEuroDat function
     * registers the asset under the name of the localAssetId
     * @param localAssetID the dataland asset id
     */
    fun getAssetFromEuroDatCatalog(localAssetID: String): EuroDaTAssetLocation {
        context.monitor.info("Searching for asset $localAssetID in EuroDaT catalog")
        var index = 0
        do {
            val catalogPage = retrieveEuroDatCatalog(
                index * Constants.EURODAT_CATALOG_PAGE_SIZE,
                (index + 1) * Constants.EURODAT_CATALOG_PAGE_SIZE
            )
            index++
            val result = catalogPage.contractOffers.firstOrNull { it.asset.properties["assetName"] == localAssetID }
            if (result != null) {
                return EuroDaTAssetLocation(
                    contractOfferId = result.id,
                    assetId = result.asset.properties["asset:prop:id"].toString()
                )
            }
        } while (catalogPage.contractOffers.isNotEmpty())
        context.monitor.severe("Could not locate asset $localAssetID in EuroDaT catalog")
        throw EdcException("Could not locate asset $localAssetID in EuroDaT catalog")
    }

    /**
     * Requests an asset from EuroDaT using the euroDatAssetId retrieved from the catalog,
     * a fresh contract id for a read-contract regarding the asset, and a targetURL.
     * EuroDaT will then HTTP-POST the asset to the targetURL
     * @param euroDatAssetId the EuroDaT asset id
     * @param retrievalContractId the contract made to retrieve the asset
     * @param targetURL the URl where the asset is supposed to be sent to
     */
    @Suppress("kotlin:S138")
    fun requestData(euroDatAssetId: String, retrievalContractId: String, targetURL: String) {
        val dataDestination = DataAddress.Builder.newInstance()
            .property("type", "")
            .property("baseUrl", targetURL)
            .build()

        val dataRequest = DataRequest.Builder.newInstance()
            .id("process-id:${UUID.randomUUID()}")
            .connectorAddress(connectorAddressEuroDat)
            .protocol(Constants.PROTOCOL_IDS_MULTIPART)
            .connectorId(Constants.CONNECTOR_ID_CONSUMER)
            .assetId(euroDatAssetId)
            .contractId(retrievalContractId)
            .dataDestination(dataDestination)
            .managedResources(false)
            .properties(
                mapOf(
                    "type" to Constants.TYPE_HTTP_FV,
                    "endpoint" to targetURL
                )
            )
            .build()

        val transferId = transferProcessManager.initiateConsumerRequest(dataRequest).content
        AwaitUtils.awaitTransferCompletion(transferProcessStore, transferId)
    }

    private fun buildAssetPolicyForUse(assetId: String): Policy {
        val action = Action.Builder.newInstance()
            .type(Constants.ACTION_TYPE_USE)
            .build()

        val assetPermission = Permission.Builder.newInstance()
            .target(assetId)
            .action(action)
            .build()

        val assetPolicy = Policy.Builder.newInstance()
            .target(assetId)
            .permission(assetPermission)
            .build()

        return assetPolicy
    }

    /**
     * Negotiates a read contract for the specified asset
     * @param assetLocation the location of the asset the contract is for
     */
    fun negotiateReadContract(assetLocation: EuroDaTAssetLocation): ContractAgreement {
        val useAssetPolicy = buildAssetPolicyForUse(assetLocation.assetId)

        val assetContractOffer = ContractOffer.Builder.newInstance()
            .id(assetLocation.contractOfferId)
            .assetId(assetLocation.assetId)
            .policy(useAssetPolicy)
            .provider(URI(Constants.URN_KEY_PROVIDER))
            .consumer(URI(Constants.URN_KEY_CONSUMER))
            .build()

        val contractOfferRequest = ContractOfferRequest.Builder.newInstance()
            .type(ContractOfferRequest.Type.INITIAL)
            .connectorId(Constants.CONNECTOR_ID_PROVIDER)
            .connectorAddress(connectorAddressEuroDat)
            .protocol(Constants.PROTOCOL_IDS_MULTIPART)
            .contractOffer(assetContractOffer)
            .build()

        val negotiation = consumerContractNegotiationManager.initiate(contractOfferRequest).content
        return AwaitUtils.awaitContractConfirm(contractNegotiationStore, negotiation)
    }

    /**
     * Negotiates a use contract for the Asset for Asset management - A Meta Asset
     * used for asset management tasks
     */
    @Suppress("kotlin:S138")
    fun negotiateAssetForAssetManagementContract(): ContractNegotiation {
        val useAssetPolicy = buildAssetPolicyForUse(Constants.ASSET_ID_ASSET_FOR_ASSET_MANAGEMENT)

        val asset = Asset.Builder.newInstance()
            .id(Constants.ASSET_ID_ASSET_FOR_ASSET_MANAGEMENT)
            .build()

        val assetContractOffer = ContractOffer.Builder.newInstance()
            .id("contract-def-id:${UUID.randomUUID()}")
            .asset(asset)
            .policy(useAssetPolicy)
            .provider(URI(Constants.URN_KEY_PROVIDER))
            .consumer(URI(Constants.URN_KEY_CONSUMER))
            .build()

        val contractOfferRequest = ContractOfferRequest.Builder.newInstance()
            .type(ContractOfferRequest.Type.INITIAL)
            .connectorId(Constants.CONNECTOR_ID_PROVIDER)
            .connectorAddress(connectorAddressEuroDat)
            .protocol(Constants.PROTOCOL_IDS_MULTIPART)
            .contractOffer(assetContractOffer)
            .build()
        return consumerContractNegotiationManager.initiate(contractOfferRequest).content
    }
}
