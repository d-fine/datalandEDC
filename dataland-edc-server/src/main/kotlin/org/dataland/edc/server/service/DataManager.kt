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
import org.eclipse.dataspaceconnector.spi.types.domain.contract.negotiation.ContractNegotiationStates
import org.eclipse.dataspaceconnector.spi.types.domain.contract.negotiation.ContractOfferRequest
import org.eclipse.dataspaceconnector.spi.types.domain.contract.offer.ContractDefinition
import org.eclipse.dataspaceconnector.spi.types.domain.contract.offer.ContractOffer
import org.eclipse.dataspaceconnector.spi.types.domain.transfer.DataRequest
import org.eurodat.broker.model.ProviderRequest
import java.net.URI
import java.time.Duration
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

private const val PROVIDER_URN_KEY = "urn:connector:provider"
private const val CONSUMER_URN_KEY = "urn:connector:consumer"

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
    private val context: ServiceExtensionContext
) {
    companion object {
        private val timeout = Duration.ofSeconds(60)
        private val pollInterval = Duration.ofMillis(100)
    }

    private val trusteeURL = context.getSetting("trustee.uri", "default")
    private val trusteeIdsURL = context.getSetting("trustee.ids.uri", "default")

    private val datalandEdcServerUrl = "http://" + context.getSetting("edc.server.uri", "default") + ":9191"
    private val datalandEdcServerIdsUrl = "http://" + context.getSetting("edc.server.uri", "default") + ":9292"

    private val trusteeClient = TrusteeClient(trusteeURL, context.getSetting("trustee.credentials", "password"))

    private val receivedAssets: ConcurrentHashMap<String, String> = ConcurrentHashMap()
    private val providedAssets: ConcurrentHashMap<String, String> = ConcurrentHashMap()

    private val endpointForAssetPickup = "$datalandEdcServerUrl/api/dataland/eurodat/asset"
    private val participantId = "dataland"
    private val datalandConnectorAddress = "$datalandEdcServerIdsUrl/api/v1/ids/data"
    private val dataOwnerId = "dataland"
    private val storageType = "persistent"

    private val dummyDatalandAssetId = "test-asset"
    private val dummyPolicyUid = "956e172f-2de1-4501-8881-057a57fd0e60"
    private val dummyActionType = "USE"
    private val dummyAction = Action.Builder.newInstance().type(dummyActionType).build()
    private val dummyPermission = Permission.Builder.newInstance().target(dummyDatalandAssetId)
        .action(dummyAction).build()
    private val dummyPolicy = Policy.Builder.newInstance().id(dummyPolicyUid).permission(dummyPermission).build()
    private val dummyAsset = Asset.Builder.newInstance().build()

    // This is a workaround to enable multiple asset upload even-though EuroDaT supports only the ID 1 (see DALA-146)
    private val dummyContractDefinition = ContractDefinition.Builder.newInstance()
        .id("1")
        .accessPolicy(dummyPolicy)
        .contractPolicy(dummyPolicy)
        .selectorExpression(
            AssetSelectorExpression.Builder.newInstance().whenEquals(Asset.PROPERTY_ID, dummyDatalandAssetId).build()
        )
        .build()

    init {
        contractDefinitionStore.save(dummyContractDefinition)
    }

    private fun buildProviderRequest(asset: Asset): ProviderRequest {
        return ProviderRequest(
            participantId = participantId,
            participantConnectorAddress = datalandConnectorAddress,
            ownerId = dataOwnerId,
            contentType = storageType,
            asset = asset,
            policy = dummyPolicy,
            provider = URI(PROVIDER_URN_KEY),
            consumer = URI(CONSUMER_URN_KEY)
        )
    }

    private fun registerAssetLocally(data: String): Pair<Asset, String> {
        val datalandAssetId = UUID.randomUUID().toString()
        providedAssets[datalandAssetId] = data

        val asset = Asset.Builder.newInstance().id(dummyDatalandAssetId)
            .property("endpoint", "$endpointForAssetPickup/$datalandAssetId").build()

        val dataAddress = DataAddress.Builder.newInstance().type("Http")
            .property("endpoint", "$endpointForAssetPickup/$datalandAssetId").build()

        assetLoader.accept(asset, dataAddress)
        return Pair(asset, datalandAssetId)
    }

    /**
     * Method to store data as an asset in the trustee
     * @param data the data to be stored in the trustee
     */
    fun provideAssetToTrustee(data: String): String {
        val (asset, datalandAssetId) = registerAssetLocally(data)
        context.monitor.info("Asset successfully registered with Dataland EDC.")
        val trusteeResponse = trusteeClient.registerAsset(buildProviderRequest(asset))
        providedAssets.remove(datalandAssetId)
        context.monitor.info("Asset successfully registered with Trustee.")
        val trusteeAssetId = trusteeResponse["asset"]["properties"]["asset:prop:id"].asText()
        val contractDefinitionId = trusteeResponse["contractDefinition"]["id"].asText()
        return "$trusteeAssetId:$contractDefinitionId"
    }

    /**
     * Method to make data available for pickup from trustee
     * @param datalandAssetId ID given to the asset on Dataland EDC side
     */
    fun getProvidedAsset(datalandAssetId: String): String {
        context.monitor.info("Received request for data with ID: $datalandAssetId")
        return providedAssets[datalandAssetId] ?: "No data with ID $datalandAssetId found."
    }

    private fun retrieveAssetFromTrustee(trusteeAssetId: String, contractDefinitionId: String): String {
        val negotiationId = initiateNegotiations(trusteeAssetId, contractDefinitionId)
        val agreementId = getAgreementId(negotiationId)
        requestData(agreementId, trusteeAssetId)
        return getReceivedAsset(trusteeAssetId)
    }

    private fun requestData(agreementId: String, trusteeAssetId: String) {
        val dataDestination = DataAddress.Builder.newInstance()
            .property("type", "HttpFV")
            .property("endpoint", "$datalandEdcServerUrl/api/dataland/eurodat/asset")
            .build()
        val dataRequest = DataRequest.Builder.newInstance()
            .id("process-id:$agreementId")
            .connectorAddress("$trusteeIdsURL/v1/ids/data")
            .protocol("ids-multipart")
            .connectorId("consumer")
            .assetId(trusteeAssetId)
            .contractId(agreementId)
            .dataDestination(dataDestination)
            .managedResources(false)
            .build()
        transferProcessManager.initiateConsumerRequest(dataRequest)
    }

    private fun getAgreementId(negotiationId: String): String {
        await()
            .atMost(timeout)
            .pollInterval(pollInterval)
            .until {
                ContractNegotiationStates.from(contractNegotiationStore.find(negotiationId)!!.state) ==
                    ContractNegotiationStates.CONFIRMED
            }
        return contractNegotiationStore.find(negotiationId)!!.contractAgreement.id
    }

    private fun initiateNegotiations(trusteeAssetId: String, contractDefinitionId: String): String {
        val assetPermission = Permission.Builder.newInstance().target(trusteeAssetId).action(dummyAction).build()
        val assetPolicy = Policy.Builder.newInstance().id(dummyPolicyUid).permission(assetPermission).build()
        val assetContractOffer = ContractOffer.Builder.newInstance()
            .id("$contractDefinitionId:3a75736e-001d-4364-8bd4-9888490edb59")
            .policy(assetPolicy)
            .asset(dummyAsset)
            .provider(URI(PROVIDER_URN_KEY))
            .consumer(URI(PROVIDER_URN_KEY))
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

    private fun getReceivedAsset(trusteeAssetId: String): String {
        await()
            .atMost(timeout)
            .pollInterval(pollInterval)
            .until {
                receivedAssets.containsKey(trusteeAssetId)
            }
        return receivedAssets[trusteeAssetId] ?: "No data under ID $trusteeAssetId found."
    }

    /**
     * Stores given data under a given asset ID in the in memory store
     * @param trusteeAssetId uuid as provided by the trustee
     * @param data the data to be stored in string format
     */
    fun storeReceivedAsset(trusteeAssetId: String, data: String) {
        context.monitor.info("Received and stored data with ID: $trusteeAssetId")
        receivedAssets[trusteeAssetId] = data
    }

    /**
     * Retrieve data for given data ID from in memory store or from the trustee if it is not yet in memory
     * @param dataId The identifier to uniquely determine the data in question
     */
    fun getDataById(dataId: String): String {
        val splitDataId = dataId.split(":")
        if (splitDataId.size != 2) throw IllegalArgumentException("The data ID $dataId has an invalid format.")
        val trusteeAssetId = splitDataId[0]

        return if (receivedAssets.containsKey(trusteeAssetId)) {
            receivedAssets[trusteeAssetId]!!
        } else {
            retrieveAssetFromTrustee(trusteeAssetId = trusteeAssetId, contractDefinitionId = splitDataId[1])
        }
    }
}
