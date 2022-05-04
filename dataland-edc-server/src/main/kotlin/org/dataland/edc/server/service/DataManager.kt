package org.dataland.edc.server.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.dataland.edc.server.models.DALADefaultOkHttpClientFactoryImpl
import org.dataland.edc.server.models.DALAHttpClient
import org.eclipse.dataspaceconnector.dataloading.AssetLoader
import org.eclipse.dataspaceconnector.policy.model.Action
import org.eclipse.dataspaceconnector.policy.model.Permission
import org.eclipse.dataspaceconnector.policy.model.Policy
import org.eclipse.dataspaceconnector.spi.asset.AssetSelectorExpression
import org.eclipse.dataspaceconnector.spi.contract.offer.store.ContractDefinitionStore
import org.eclipse.dataspaceconnector.spi.system.ServiceExtensionContext
import org.eclipse.dataspaceconnector.spi.types.domain.DataAddress
import org.eclipse.dataspaceconnector.spi.types.domain.asset.Asset
import org.eclipse.dataspaceconnector.spi.types.domain.contract.offer.ContractDefinition
import org.eclipse.dataspaceconnector.spi.types.domain.contract.offer.ContractOffer
import org.eclipse.dataspaceconnector.spi.types.domain.transfer.DataRequest
import org.eurodat.broker.model.ProviderRequest
import java.net.URI

class DataManager(private val assetLoader: AssetLoader, private val contractDefinitionStore: ContractDefinitionStore, private val context: ServiceExtensionContext) {

    private val trusteeURL = "http://20.31.200.61:80/api"
    private val trusteeIdsURL = "http://20.31.200.61:80/api"

    private val datalandEdcServerUrl = "http://"+context.getSetting("TUNNEL_URI", "default")+":9191"
    private val datalandEdcServerIdsURL = "http://"+context.getSetting("TUNNEL_URI", "default")+":9292"

    private val testCredentials = "password"

    private val trusteeClient = DALAHttpClient(
        DALADefaultOkHttpClientFactoryImpl.create(false), trusteeURL, "APIKey", testCredentials
    )
    private val datalandConnectorClient = DALAHttpClient(
        DALADefaultOkHttpClientFactoryImpl.create(false), datalandEdcServerUrl, "APIKey", testCredentials
    )

    private val jsonMapper = jacksonObjectMapper()

    private val receivedAssets: MutableMap<String, String> = mutableMapOf()
    private val providedAssets: MutableMap<String, String> = mutableMapOf()

    private var counter = 0

    private val dummyProviderAssetId = "test-asset"
    private val dummyPolicyUid = "956e172f-2de1-4501-8881-057a57fd0e60"
    private val dummyActionType = "USE"
    private val dummyAction = Action.Builder.newInstance().type(dummyActionType).build()
    private val dummyPermission = Permission.Builder.newInstance().target(dummyProviderAssetId).action(dummyAction).build()
    private val dummyPolicy = Policy.Builder.newInstance().id(dummyPolicyUid).permission(dummyPermission).build()
    private val dummyAsset = Asset.Builder.newInstance().build()

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

    private fun registerAsset(
        data: String
    ): Asset {
        val providerAssetId = generateProviderAssetId()
        providedAssets[providerAssetId] = data

        val asset = Asset.Builder.newInstance().id(dummyProviderAssetId)
            .property("endpoint", "$datalandEdcServerUrl/api/dataland/provideAsset/$providerAssetId").build()

        val dataAddress = DataAddress.Builder.newInstance()
            .type("Http")
            .property("endpoint", "$datalandEdcServerUrl/api/dataland/provideAsset/$providerAssetId").build()

        assetLoader.accept(asset, dataAddress)
        return asset
    }

    fun uploadAssetToEuroDaT(data: String): String {
        val asset = registerAsset(data)
        val providerRequestString = jsonMapper.writeValueAsString(buildProviderRequest(asset))
        val assetResponse = trusteeClient.post("/asset/register", providerRequestString)
        val assetId = assetResponse["asset"]["properties"]["asset:prop:id"].asText()
        val contractDefinitionId = assetResponse["contractDefinition"]["id"].asText()
        return "$assetId:$contractDefinitionId"
    }

    fun getProvidedAsset(assetId: String): String {
        return providedAssets[assetId] ?: "No data with assetId $assetId found"
    }

    fun getAssetFromEuroDaT(assetId: String, contractDefinitionId: String): String {
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
        val dataRequestString = jsonMapper.writeValueAsString(dataRequest)
        datalandConnectorClient.post("/api/control/transfer", dataRequestString)
    }

    private fun getAgreementId(negotiationId: String): String {
        var agreementId: String? = null
        while (agreementId == null) {
            val checkNegotiationResult = datalandConnectorClient.get("/api/control/negotiation/$negotiationId/state")
            if (checkNegotiationResult["status"].asText() == "CONFIRMED") {
                agreementId = checkNegotiationResult["contractAgreementId"].asText()
            }
            Thread.sleep(3000)
        }
        return agreementId
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

        val consumerRequestString = jsonMapper.writeValueAsString(assetContractOffer)
        val params = mapOf("Content-Type" to "application/json", "connectorAddress" to "$trusteeIdsURL/v1/ids/data")
        val negotiationResponse = datalandConnectorClient.post("/api/negotiation", consumerRequestString, params)
        return negotiationResponse["id"].asText()
    }

    private fun getReceivedAsset(assetId: String): String {
        var timeout = 60
        while (!receivedAssets.containsKey(assetId)) {
            println("Requested data for $assetId not found. Waiting.")
            Thread.sleep(5000)
            if (timeout > 0) timeout -= 5 else break
        }
        return receivedAssets[assetId] ?: "Data not found"
    }

    fun storeReceivedAsset(id: String, decodedData: String) {
        receivedAssets[id] = decodedData
    }
}
