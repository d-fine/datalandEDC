package org.dataland.edc.server.service

import org.dataland.edc.server.extensions.AssetForAssetManagementContractExtension
import org.dataland.edc.server.utils.AwaitUtils
import org.dataland.edc.server.utils.Constants
import org.eclipse.dataspaceconnector.policy.model.Action
import org.eclipse.dataspaceconnector.policy.model.Permission
import org.eclipse.dataspaceconnector.policy.model.Policy
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
import org.eclipse.dataspaceconnector.spi.types.domain.contract.negotiation.ContractOfferRequest
import org.eclipse.dataspaceconnector.spi.types.domain.contract.offer.ContractOffer
import org.eclipse.dataspaceconnector.spi.types.domain.transfer.DataRequest
import java.net.URI
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class DataManager(
    private val transferProcessManager: TransferProcessManager,
    private val contractNegotiationStore: ContractNegotiationStore,
    private val transferProcessStore : TransferProcessStore,
    private val consumerContractNegotiationManager: ConsumerContractNegotiationManager,
    private val context: ServiceExtensionContext,
    private val dispatcher : RemoteMessageDispatcherRegistry,
) {
    private val providedAssets: ConcurrentHashMap<String, String> = ConcurrentHashMap()
    private val receivedAssets: ConcurrentHashMap<String, String> = ConcurrentHashMap()

    /**
     * Method to make data available for pickup from trustee
     * @param datalandAssetId ID given to the asset on Dataland EDC side
     */
    fun getProvidedAsset(datalandAssetId: String): String {
        context.monitor.info("Received request for data with ID: $datalandAssetId")
        return providedAssets[datalandAssetId] ?: "No data with ID $datalandAssetId found."
    }

    fun provideAssetToTrustee(data : String) : String {
        val localAssetId = registerAssetLocally(data);
        registerAssetEuroDat(localAssetId)
        val (offerId, euroDaTAssetId) = getAssetFromEuroDatCatalog(localAssetId)
        return "${offerId}_${euroDaTAssetId}"
    }

    private fun getAssetFromEuroDatCatalog(localAssetID: String) : Pair<String, String>  {
        val request = CatalogRequest.Builder.newInstance()
            .protocol(Constants.PROTOCOL_IDS_MULTIPART)
            .connectorId(Constants.CONNECTOR_ID_PROVIDER)
            .connectorAddress(Constants.CONNECTOR_ADDRESS_EURODAT)
            .range(Range(0, Integer.MAX_VALUE))
            .build()

        val catalogFuture = dispatcher.send(Catalog::class.java, request) { null }
        val catalog = catalogFuture.join()

        val result = catalog.contractOffers.firstOrNull() { it.asset.properties["assetName"] == localAssetID }!!
        return Pair(result.id, result.asset.properties["asset:prop:id"].toString())
    }

    /**
     * Retrieve data for given data ID from in memory store or from the trustee if it is not yet in memory
     * @param dataId The identifier to uniquely determine the data in question
     */
    fun getDataById(dataId: String): String {
        val splitDataId = dataId.split("_")
        if (splitDataId.size != 2) throw IllegalArgumentException("The data ID $dataId has an invalid format.")
        val contractDefId = splitDataId[0]
        val trusteeAssetId = splitDataId[1]

        return if (receivedAssets.containsKey(trusteeAssetId)) {
            receivedAssets[trusteeAssetId]!!
        } else {
           retrieveAssetFromTrustee(trusteeAssetId = trusteeAssetId, contractDefinitionId = contractDefId)
        }
    }

    private fun requestData(agreementId: String, trusteeAssetId: String) {
        val dataDestination = DataAddress.Builder.newInstance()
            .property("type", "")
            .property("baseUrl", getLocalAssetAccessURl(trusteeAssetId))
            .build()
        val dataRequest = DataRequest.Builder.newInstance()
            .id("process-id:${UUID.randomUUID()}")
            .connectorAddress(Constants.CONNECTOR_ADDRESS_EURODAT)
            .protocol(Constants.PROTOCOL_IDS_MULTIPART)
            .connectorId(Constants.CONNECTOR_ID_CONSUMER)
            .assetId(trusteeAssetId)
            .contractId(agreementId)
            .dataDestination(dataDestination)
            .managedResources(false)
            .properties(mapOf(
                "type" to "HttpFV",
                "endpoint" to getLocalAssetAccessURl(trusteeAssetId)
            ))
            .build()
        val transferId = transferProcessManager.initiateConsumerRequest(dataRequest).content
        AwaitUtils.awaitTransferCompletion(transferProcessStore, transferId)
    }
    private fun retrieveAssetFromTrustee(trusteeAssetId: String, contractDefinitionId: String): String {
        val agreement = initiateNegotiations(trusteeAssetId, contractDefinitionId)
        requestData(agreement.id, trusteeAssetId)
        return ""
    }

    private fun initiateNegotiations(trusteeAssetId: String, contractDefinitionId: String): ContractAgreement {
        val action = Action.Builder.newInstance()
            .type(Constants.ACTION_TYPE_USE)
            .build()

        val assetPermission = Permission.Builder.newInstance()
            .target(trusteeAssetId)
            .action(action)
            .build()

        val assetPolicy = Policy.Builder.newInstance()
            .target(trusteeAssetId)
            .permission(assetPermission)
            .build()

        val assetContractOffer = ContractOffer.Builder.newInstance()
            .id(contractDefinitionId)
            .assetId(trusteeAssetId)
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


        val negotiation = consumerContractNegotiationManager.initiate(contractOfferRequest).content
        return AwaitUtils.awaitContractConfirm(contractNegotiationStore, negotiation);
    }

    private fun getLocalAssetAccessURl(localAssetID: String) : String {
        return "${Constants.BASE_ADDRESS_DATALAND_TO_EURODAT_API}/asset/$localAssetID"
    }

    private fun registerAssetLocally(data: String): String {
        val datalandAssetId = UUID.randomUUID().toString()
        providedAssets[datalandAssetId] = data
        context.monitor.info("Registered new local asset under ID $datalandAssetId)")
        return datalandAssetId
    }

    fun registerAssetEuroDat(datalandAssetId: String) {
        context.monitor.info("Registering asset $datalandAssetId with EuroDat")
        val assetForAssetManagementContractConfirmation = AwaitUtils.awaitContractConfirm(
            contractNegotiationStore,
            AssetForAssetManagementContractExtension.assetForAssetManagementNegotiation!!
        )

        val dummyDataDestination = DataAddress.Builder.newInstance()
            .type("")
            .property("endpoint", "unused-endpoint")
            .build()

        val dataRequest = DataRequest.Builder.newInstance()
            .id("process-id:$datalandAssetId")
            .protocol(Constants.PROTOCOL_IDS_MULTIPART)
            .connectorAddress(Constants.CONNECTOR_ADDRESS_EURODAT)
            .connectorId(Constants.CONNECTOR_ID_PROVIDER)
            .assetId(Constants.ASSET_ID_ASSET_FOR_ASSET_MANAGEMENT)
            .contractId(assetForAssetManagementContractConfirmation.id)
            .dataDestination(dummyDataDestination)
            .managedResources(false)
            .properties(mapOf(
                "type" to Constants.TYPE_HTTP_ASSET_REGISTRATION,
                "endpoint" to getLocalAssetAccessURl(datalandAssetId),
                "providerId" to Constants.PROVIDER_ID_DATALAND,
                "ownerId" to Constants.OWNER_ID_DATALAND,
                "contentType" to Constants.CONTENT_TYPE_PERSISTENT,
                "policyTemplateId" to Constants.POLICY_TEMPLATE_ID,
                "assetName" to datalandAssetId,
                "providerAssetId" to "",
                "queryAgreementId" to "",
            ))
            .build()
        val transferId = transferProcessManager.initiateConsumerRequest(dataRequest).content
        AwaitUtils.awaitTransferCompletion(transferProcessStore, transferId)


    }


}
