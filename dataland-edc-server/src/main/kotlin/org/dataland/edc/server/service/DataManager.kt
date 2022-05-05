package org.dataland.edc.server.service

import org.awaitility.Awaitility.await
import org.dataland.edc.server.models.TrusteeClient
import org.eclipse.dataspaceconnector.dataloading.AssetLoader
import org.eclipse.dataspaceconnector.policy.model.Action
import org.eclipse.dataspaceconnector.policy.model.Permission
import org.eclipse.dataspaceconnector.policy.model.Policy
import org.eclipse.dataspaceconnector.spi.asset.AssetSelectorExpression
import org.eclipse.dataspaceconnector.spi.contract.negotiation.ConsumerContractNegotiationManager
import org.eclipse.dataspaceconnector.spi.contract.negotiation.store.ContractNegotiationStore
import org.eclipse.dataspaceconnector.spi.contract.offer.store.ContractDefinitionStore
import org.eclipse.dataspaceconnector.spi.system.ServiceExtensionContext
import org.eclipse.dataspaceconnector.spi.transfer.TransferProcessManager
import org.eclipse.dataspaceconnector.spi.types.domain.DataAddress
import org.eclipse.dataspaceconnector.spi.types.domain.asset.Asset
import org.eclipse.dataspaceconnector.spi.types.domain.contract.negotiation.ContractNegotiation
import org.eclipse.dataspaceconnector.spi.types.domain.contract.negotiation.ContractNegotiationStates
import org.eclipse.dataspaceconnector.spi.types.domain.contract.negotiation.ContractOfferRequest
import org.eclipse.dataspaceconnector.spi.types.domain.contract.offer.ContractDefinition
import org.eclipse.dataspaceconnector.spi.types.domain.contract.offer.ContractOffer
import org.eclipse.dataspaceconnector.spi.types.domain.transfer.DataRequest
import org.eurodat.broker.model.ProviderRequest
import java.net.URI
import java.time.Duration

/**
 * Entity orchestrating the required steps for trustee data exchange
 * @param assetLoader holds all registered assets of the Dataland EDC
 * @param contractDefinitionStore holds all available contracts of the Dataland EDC
 * @param transferProcessManager manages the transfer process
 * @param contractNegotiationStore holds the contract negotiations of the Dataland EDC
 * @param consumerContractNegotiationManager manages contract negotiations
 * @param context the context containing constants and the monitor for logging
 */
class DataManager(
    private val assetLoader: AssetLoader,
    contractDefinitionStore: ContractDefinitionStore,
    private val transferProcessManager: TransferProcessManager,
    private val contractNegotiationStore: ContractNegotiationStore,
    private val consumerContractNegotiationManager: ConsumerContractNegotiationManager,
    context: ServiceExtensionContext
) {
    companion object {
        private val timeout = Duration.ofSeconds(60)
        private val pollInterval = Duration.ofMillis(100)
    }

    private val trusteeURL = context.getSetting("trustee.uri", "default")
    private val trusteeIdsURL = context.getSetting("trustee.ids.uri", "default")

    private val datalandEdcServerUrl = "http://" + context.getSetting("edc.server.uri", "default") + ":9191"
    private val datalandEdcServerIdsURL = "http://" + context.getSetting("edc.server.uri", "default") + ":9292"

    private val testCredentials = context.getSetting("trustee.credentials", "default")

    private val trusteeClient = TrusteeClient(trusteeURL, testCredentials)

    private val receivedAssets: MutableMap<String, String> = mutableMapOf()
    private val providedAssets: MutableMap<String, String> = mutableMapOf()

    private var counter = 0

    private val dummyProviderAssetId = "test-asset"
    private val dummyPolicyUid = "956e172f-2de1-4501-8881-057a57fd0e60"
    private val dummyActionType = "USE"
    private val dummyAction = Action.Builder.newInstance().type(dummyActionType).build()
    private val dummyPermission = Permission.Builder.newInstance().target(dummyProviderAssetId)
        .action(dummyAction).build()
    private val dummyPolicy = Policy.Builder.newInstance().id(dummyPolicyUid).permission(dummyPermission).build()
    private val dummyAsset = Asset.Builder.newInstance().build()

    // This is a workaround to enable multiple asset upload even-though EuroDaT supports only the ID 1 (see DALA-146)
    private val dummyContractDefinition = ContractDefinition.Builder.newInstance()
        .id("1")
        .accessPolicy(dummyPolicy)
        .contractPolicy(dummyPolicy)
        .selectorExpression(
            AssetSelectorExpression.Builder.newInstance().whenEquals(Asset.PROPERTY_ID, dummyProviderAssetId).build()
        )
        .build()

    init {
        contractDefinitionStore.save(dummyContractDefinition)
    }

    private fun generateProviderAssetId(): String {
        counter += 1
        return counter.toString()
    }

    private fun buildProviderRequest(asset: Asset): ProviderRequest {
        return ProviderRequest(
            "eurodat-connector-test",
            "$datalandEdcServerIdsURL/api/v1/ids/data",
            "owner-ID",
            "persistent",
            asset, dummyPolicy, URI("urn:connector:provider"), URI("urn:connector:consumer")
        )
    }

    private fun registerAsset(data: String): Asset {
        val providerAssetId = generateProviderAssetId()
        providedAssets[providerAssetId] = data

        val asset = Asset.Builder.newInstance().id(dummyProviderAssetId)
            .property("endpoint", "$datalandEdcServerUrl/api/dataland/provideAsset/$providerAssetId").build()

        val dataAddress = DataAddress.Builder.newInstance().type("Http")
            .property("endpoint", "$datalandEdcServerUrl/api/dataland/provideAsset/$providerAssetId").build()

        assetLoader.accept(asset, dataAddress)
        return asset
    }

    /**
     * Methode to store data as an asset in the trustee
     * @param data the data to be stored in the trustee
     */
    fun provideAssetToTrustee(data: String): String {
        val asset = registerAsset(data)
        val trusteeResponse = trusteeClient.registerAsset(buildProviderRequest(asset))
        val trusteeAssetId = trusteeResponse["asset"]["properties"]["asset:prop:id"].asText()
        val contractDefinitionId = trusteeResponse["contractDefinition"]["id"].asText()
        return "$trusteeAssetId:$contractDefinitionId"
    }

    /**
     * Methode to make data available for pickup from trustee
     * @param providerAssetId ID given to the asset on Dataland EDC side
     */
    fun getProvidedAsset(providerAssetId: String): String {
        return providedAssets.remove(providerAssetId) ?: "No data with assetId $providerAssetId found."
    }

    private fun retrieveAssetFromTrustee(assetId: String, contractDefinitionId: String): String {
        val negotiationId = initiateNegotiations(assetId, contractDefinitionId)
        val agreementId = getAgreementId(negotiationId)
        requestData(agreementId, assetId)
        return getReceivedAsset(assetId)
    }

    private fun requestData(agreementId: String, assetId: String) {
        val dataDestination = DataAddress.Builder.newInstance()
            .property("type", "HttpFV")
            .property("endpoint", "$datalandEdcServerUrl/api/dataland/receiveAsset")
            .build()
        val dataRequest = DataRequest.Builder.newInstance()
            .id("process-id:$agreementId")
            .connectorAddress("$trusteeIdsURL/v1/ids/data")
            .protocol("ids-multipart")
            .connectorId("consumer")
            .assetId(assetId)
            .contractId(agreementId)
            .dataDestination(dataDestination)
            .managedResources(false)
            .build()
        transferProcessManager.initiateConsumerRequest(dataRequest)
    }

    private fun getAgreementId(negotiationId: String): String {
        val negotiation: ContractNegotiation = contractNegotiationStore.find(negotiationId)!!
        await()
            .atMost(timeout)
            .pollInterval(pollInterval)
            .until {
                ContractNegotiationStates.from(negotiation.state) == ContractNegotiationStates.CONFIRMED
            }
        return negotiation.contractAgreement.id
    }

    private fun initiateNegotiations(assetId: String, contractDefinitionId: String): String {
        val assetPermission = Permission.Builder.newInstance().target(assetId).action(dummyAction).build()
        val assetPolicy = Policy.Builder.newInstance().id(dummyPolicyUid).permission(assetPermission).build()
        val assetContractOffer = ContractOffer.Builder.newInstance()
            .id("$contractDefinitionId:3a75736e-001d-4364-8bd4-9888490edb59")
            .policy(assetPolicy)
            .asset(dummyAsset)
            .provider(URI("urn:connector:provider"))
            .consumer(URI("urn:connector:provider"))
            .build()

        val contractOfferRequest = ContractOfferRequest.Builder.newInstance()
            .contractOffer(assetContractOffer)
            .protocol("ids-multipart")
            .connectorId("consumer")
            .connectorAddress("$trusteeIdsURL/v1/ids/data")
            .type(ContractOfferRequest.Type.INITIAL)
            .build()

        return consumerContractNegotiationManager.initiate(contractOfferRequest).content.id
    }

    private fun getReceivedAsset(assetId: String): String {
        await()
            .atMost(timeout)
            .pollInterval(pollInterval)
            .until {
                receivedAssets.containsKey(assetId)
            }
        return receivedAssets[assetId] ?: "Data not found"
    }

    /**
     * Stores given data under a given asset ID in the in memory store
     * @param assetId uuid as provided by the trustee
     * @param data the data to be stored in string format
     */
    fun storeReceivedAsset(assetId: String, data: String) {
        receivedAssets[assetId] = data
    }

    /**
     * Retrieve data for given data ID from in memory store or from the trustee if it is not yet in memory
     * @param dataId The identifier to uniquely determine the data in question
     */
    fun getDataById(dataId: String): String {
        val splitDataId = dataId.split(":")
        if (splitDataId.size != 2) throw IllegalArgumentException("The data ID $dataId has an invalid format.")
        val assetId = splitDataId[0]

        return if (receivedAssets.containsKey(assetId)) {
            receivedAssets[assetId]!!
        } else {
            retrieveAssetFromTrustee(assetId = assetId, contractDefinitionId = splitDataId[1])
        }
    }
}
