package org.eclipse.dataspaceconnector.extensions.controller

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.eclipse.dataspaceconnector.extensions.models.DALADefaultOkHttpClientFactoryImpl
import org.eclipse.dataspaceconnector.extensions.models.DALAHttpClient
import org.eclipse.dataspaceconnector.policy.model.Action
import org.eclipse.dataspaceconnector.policy.model.Permission
import org.eclipse.dataspaceconnector.policy.model.Policy
import org.eclipse.dataspaceconnector.spi.types.domain.DataAddress
import org.eclipse.dataspaceconnector.spi.types.domain.asset.Asset
import org.eclipse.dataspaceconnector.spi.types.domain.contract.offer.ContractOffer
import org.eclipse.dataspaceconnector.spi.types.domain.transfer.DataRequest
import org.eurodat.broker.model.ProviderRequest
import java.io.IOException
import java.net.URI

class DatalandController() {

    private val jsonMapper = jacksonObjectMapper()

    private val trusteeURL = "http://20.31.200.61:80/api"
    private val trusteeIdsURL = "http://20.31.200.61:80/api"

    private val providerURL = "http://dataland-tunnel.duckdns.org:9191"
    private val providerIdsURL = "http://dataland-tunnel.duckdns.org:9292"

    private val consumerURL = providerURL
    private val consumerIdsURL = providerIdsURL

    private val testCredentials = "password"

    private val trusteeClient = DALAHttpClient(
        DALADefaultOkHttpClientFactoryImpl.create(false), trusteeURL, "APIKey", testCredentials
    )
    private val consumerClient = DALAHttpClient(
        DALADefaultOkHttpClientFactoryImpl.create(false), consumerURL, "APIKey", testCredentials
    )

    fun buildProviderRequest(
        assetId: String = "test-asset",
        policyUid: String = "956e172f-2de1-4501-8881-057a57fd0e60",
        actionType: String = "USE"
    ): ProviderRequest {

        val action = Action.Builder.newInstance()
            .type(actionType)
            .build()
        val permission = Permission.Builder.newInstance()
            .target(assetId)
            .action(action)
            .build()
        val asset = Asset.Builder.newInstance()
            .id(assetId)
            .property("endpoint", "https://filesamples.com/samples/code/json/sample2.json")
            .build()
        val policy = Policy.Builder.newInstance()
            .id(policyUid)
            .permission(permission)
            .build()

        return ProviderRequest(
            "eurodat-connector-test",
            "$providerIdsURL/api/v1/ids/data",
            "owner-ID",
            "persistent",
            asset,
            policy,
            URI("urn:connector:provider"),
            URI("urn:connector:consumer")
        )
    }

    fun registerAsset(providerRequest: ProviderRequest): Map<String, String> {
        val providerRequestString = jsonMapper.writeValueAsString(providerRequest)
        val assetResponse = trusteeClient.post("/asset/register", providerRequestString)
        val assetId = assetResponse["asset"]["properties"]["asset:prop:id"].asText()
        val contractDefinitionId = assetResponse["contractDefinition"]["id"].asText()

        return mapOf("assetId" to assetId, "contractDefinitionId" to contractDefinitionId)
    }

    fun getAsset(
        assetId: String,
        contractDefinitionId: String,
        actionType: String = "USE",
        policyUid: String = "956e172f-2de1-4501-8881-057a57fd0e60"
    ): String {
        val action = Action.Builder.newInstance()
            .type(actionType)
            .build()
        val asset = Asset.Builder.newInstance()
            .id("test-asset")
            .property("endpoint", "https://filesamples.com/samples/code/json/sample2.json")
            .build()

        // Consumer negotiates contract to consume asset
        val newPermission = Permission.Builder.newInstance()
            .target(assetId)
            .action(action)
            .build()
        val newPolicy = Policy.Builder.newInstance()
            .id(policyUid)
            .permission(newPermission)
            .build()
        val newContractOffer = ContractOffer.Builder.newInstance()
            .id("$contractDefinitionId:3a75736e-001d-4364-8bd4-9888490edb59")
            .policy(newPolicy)
            .asset(asset)
            .provider(URI("urn:connector:provider"))
            .consumer(URI("urn:connector:provider"))
            .build()

        val consumerRequestString = jsonMapper.writeValueAsString(newContractOffer)
        val params = mapOf("Content-Type" to "application/json", "connectorAddress" to "$trusteeIdsURL/v1/ids/data")
        val negotiationResponse = consumerClient.post("/api/negotiation", consumerRequestString, params)
        val negotiationId = negotiationResponse["id"].asText()

        var agreementId: String? = null
        while (agreementId == null) {
            val checkNegotiationResult = consumerClient.get("/api/control/negotiation/$negotiationId/state")
            if (checkNegotiationResult["status"].asText() == "CONFIRMED") {
                agreementId = checkNegotiationResult["contractAgreementId"].asText()
            }
            Thread.sleep(3000)
        }

        // After successful negotiation consumer request data transfer
        val dataDestination = DataAddress.Builder.newInstance()
            .property("type", "HttpFV")
            .property("endpoint", "$consumerURL/api/transferdestination")
            .build()
        val dataRequest = DataRequest.Builder.newInstance()
            .id("process-id:$agreementId") // Use agreementId as the processId for repeatability and ensuring one process per asset and test
            .connectorAddress("$trusteeIdsURL/api/v1/ids/data")
            .protocol("ids-multipart")
            .connectorId("consumer")
            .assetId(assetId)
            .contractId(agreementId)
            .dataDestination(dataDestination)
            .managedResources(false)
            .build()
        val dataRequestString = jsonMapper.writeValueAsString(dataRequest)
        consumerClient.post("/api/control/transfer", dataRequestString)

        // Check that the data was received on Consumer side
        var transferResponse: JsonNode? = null
        var timeout = 60
        while (transferResponse == null) {
            try {
                transferResponse = consumerClient.get("/api/checkasset/$assetId")
            } catch (exception: IOException) {
                println(exception.message)
                println("Waiting 5 seconds...")
                Thread.sleep(5000)
                if (timeout > 0) timeout -= 5 else break
            }
        }
        return "done"
    }
}
