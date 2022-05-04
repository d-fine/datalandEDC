package org.dataland.edc.server.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.eclipse.dataspaceconnector.dataloading.AssetLoader
import org.eclipse.dataspaceconnector.extensions.api.ConsumerApiController
import org.dataland.edc.server.models.DALADefaultOkHttpClientFactoryImpl
import org.dataland.edc.server.models.DALAHttpClient
import org.eclipse.dataspaceconnector.policy.model.Action
import org.eclipse.dataspaceconnector.policy.model.Permission
import org.eclipse.dataspaceconnector.policy.model.Policy
import org.eclipse.dataspaceconnector.spi.asset.AssetSelectorExpression
import org.eclipse.dataspaceconnector.spi.contract.offer.store.ContractDefinitionStore
import org.eclipse.dataspaceconnector.spi.types.domain.DataAddress
import org.eclipse.dataspaceconnector.spi.types.domain.asset.Asset
import org.eclipse.dataspaceconnector.spi.types.domain.contract.offer.ContractDefinition
import org.eclipse.dataspaceconnector.spi.types.domain.contract.offer.ContractOffer
import org.eclipse.dataspaceconnector.spi.types.domain.transfer.DataRequest
import org.eurodat.broker.model.ProviderRequest
import java.net.URI




class DataManager {

    //@Inject
    //private val loader: AssetLoader? = null

    private val trusteeURL = "http://20.31.200.61:80/api"
    private val trusteeIdsURL = "http://20.31.200.61:80/api"

    private val datalandEdcServerUrl = "http://dataland-tunnel.duckdns.org:9191"
    private val datalandEdcServerIdsURL = "http://dataland-tunnel.duckdns.org:9292"

    private val testCredentials = "password"

    private val trusteeClient = DALAHttpClient(
        DALADefaultOkHttpClientFactoryImpl.create(false), trusteeURL, "APIKey", testCredentials
    )
    private val datalandConnectorClient = DALAHttpClient(
        DALADefaultOkHttpClientFactoryImpl.create(false), datalandEdcServerUrl, "APIKey", testCredentials
    )

    private val receivedAssets: MutableMap<String, String> = mutableMapOf()
    private val providedAssets: MutableMap<String, String> = mutableMapOf()

    private val jsonMapper = jacksonObjectMapper()
    private var counter = 0

    private fun generateProviderAssetId(): String {
        counter += 1
        return counter.toString()
        //return "test-asset"
        //return "another-asset"
    }

    private fun getReceivedAsset(assetId: String): String {
        var timeout = 60
        while (!receivedAssets.containsKey(assetId)) {
            println("Waiting 5 seconds...")
            Thread.sleep(5000)
            if (timeout > 0) timeout -= 5 else break
        }
        return receivedAssets[assetId] ?: "Data not found"
    }

    private fun buildProviderRequest(
        data: String,
        providerAssetId: String = "test-asset",
        policyUid: String = "956e172f-2de1-4501-8881-057a57fd0e60",
        actionType: String = "USE",
        assetLoader: AssetLoader,
        contractDefinitionStore: ContractDefinitionStore
    ): ProviderRequest {
        val dummyProviderAssetId = "test-asset"
        println("Process $providerAssetId and $dummyProviderAssetId")
        val action = Action.Builder.newInstance().type(actionType).build()


        val permission = Permission.Builder.newInstance().target(dummyProviderAssetId).action(action).build()
        providedAssets[providerAssetId] = data

        val asset = Asset.Builder.newInstance().id(dummyProviderAssetId)
            .property("endpoint", "$datalandEdcServerUrl/api/dataland/provideAsset/$providerAssetId").build()


        //val assetPath = Path.of("/tmp/provider/test-document.txt")
        val dataAddress = DataAddress.Builder.newInstance()
            .type("HttpData")
            .property("endpoint", "https://filesamples.com/samples/code/json/sample2.json")//"$datalandEdcServerUrl/api/dataland/provideAsset/$providerAssetId")
            .build()
        println("Try to load asset.")
        assetLoader.accept(asset, dataAddress)

        val policy = Policy.Builder.newInstance().id(policyUid).permission(permission).build()

        println("Try to save contract.")
        val contractDefinition = ContractDefinition.Builder.newInstance()
            .id("1")
            .accessPolicy(policy)
            .contractPolicy(policy)
            .selectorExpression(
                AssetSelectorExpression.Builder.newInstance().whenEquals(Asset.PROPERTY_ID, dummyProviderAssetId).build()
            )
            .build()
        contractDefinitionStore.save(contractDefinition)
        println("Saved contract.")

        return ProviderRequest(
            "eurodat-connector-test",
            "$datalandEdcServerIdsURL/api/v1/ids/data",
            "owner-ID",
            "persistent",
            asset, policy, URI("urn:connector:provider"), URI("urn:connector:consumer")
        )
    }

    fun uploadAssetToEuroDaT(data: String, assetLoader: AssetLoader, contractDefinitionStore: ContractDefinitionStore): String {
        val providerRequestString =
            jsonMapper.writeValueAsString(buildProviderRequest(data, providerAssetId = generateProviderAssetId(), assetLoader = assetLoader, contractDefinitionStore = contractDefinitionStore))
        val assetResponse = trusteeClient.post("/asset/register", providerRequestString)
        val assetId = assetResponse["asset"]["properties"]["asset:prop:id"].asText()
        val contractDefinitionId = assetResponse["contractDefinition"]["id"].asText()
        return "$assetId:$contractDefinitionId"
    }

    fun getProvidedAsset(assetId: String): String {
        return providedAssets[assetId] ?: "No data with assetId $assetId found"
    }

    fun getAssetFromEuroDaT(
        assetId: String,
        contractDefinitionId: String,
        actionType: String = "USE",
        policyUid: String = "956e172f-2de1-4501-8881-057a57fd0e60",
        consumerApiController: ConsumerApiController
    ): String {
        val action = Action.Builder.newInstance().type(actionType).build()
        val asset = Asset.Builder.newInstance()
            // .id("test-asset")
            // .property("endpoint", "https://filesamples.com/samples/code/json/sample2.json")
            .build()

        // Consumer negotiates contract to consume asset
        val newPermission = Permission.Builder.newInstance().target(assetId).action(action).build()
        val newPolicy = Policy.Builder.newInstance().id(policyUid).permission(newPermission).build()
        val newContractOffer = ContractOffer.Builder.newInstance()
            .id("$contractDefinitionId:3a75736e-001d-4364-8bd4-9888490edb59")
            .policy(newPolicy)
            .asset(asset)
            .provider(URI("urn:connector:provider"))
            .consumer(URI("urn:connector:provider"))
            .build()

        val consumerRequestString = jsonMapper.writeValueAsString(newContractOffer)
        val params = mapOf("Content-Type" to "application/json", "connectorAddress" to "$trusteeIdsURL/v1/ids/data")
        val negotiationResponse = datalandConnectorClient.post("/api/negotiation", consumerRequestString, params)
        val negotiationId = negotiationResponse["id"].asText()

        /*
        val negotiationResponse = consumerApiController.initiateNegotiation("$trusteeIdsURL/v1/ids/data", newContractOffer)
        //val test6 = jsonMapper.readValue<MutableMap<String, String>>(negotiationResponse.readEntity(String::class.java))
        //println(test6["id"])
        //val test5 = JSONPObject(negotiationResponse.readEntity(String::class.java))
        //val negotiationResponse2 = consumerClient.post("/api/negotiation", consumerRequestString, params)
        //val negotiationId = negotiationResponse["id"].asText()
        //val test: String = negotiationResponse.entity
        //val test = negotiationResponse.readEntity(String::class.java)
        val json = jsonMapper.writeValueAsString(negotiationResponse.readEntity(String::class.java))
        val map: Map<String, String> = jsonMapper.readValue(json)
        val id = map["id"]

        println("______________________________________________________________________________________________")
        println(" ")
        println("negotiationResponse is " + negotiationResponse)
        println("test6 is " + test6)
        println("test is " + test)
        println("json is " + json)
        println("map is " + map)
        println("id is " + id)
        println("______________________________________________________________________________________________")
*/

        var agreementId: String? = null
        while (agreementId == null) {
            val checkNegotiationResult = datalandConnectorClient.get("/api/control/negotiation/$negotiationId/state")
            if (checkNegotiationResult["status"].asText() == "CONFIRMED") {
                agreementId = checkNegotiationResult["contractAgreementId"].asText()
            }
            Thread.sleep(3000)
        }

        // After successful negotiation consumer request data transfer
        val dataDestination = DataAddress.Builder.newInstance()
            .property("type", "HttpFV")
            .property("endpoint", "$datalandEdcServerUrl/api/dataland/receiveAsset")
            .build()
        val dataRequest = DataRequest.Builder.newInstance()
            .id("process-id:$agreementId") // Use agreementId as the processId for repeatability and ensuring one process per asset and test
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

        // Check that the data was received on Consumer side

        return getReceivedAsset(assetId)
    }

    fun storeReceivedAsset(id: String, decodedData: String) {
        receivedAssets[id] = decodedData
    }
}
