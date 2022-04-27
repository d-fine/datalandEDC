package org.eclipse.dataspaceconnector.extensions.controller

import org.eurodat.brokerextension.openApiClient.api.DefaultApi
import org.eurodat.brokerextension.openApiClient.model.Action
import org.eurodat.brokerextension.openApiClient.model.Asset
import org.eurodat.brokerextension.openApiClient.model.Permission
import org.eurodat.brokerextension.openApiClient.model.Policy
import org.eurodat.brokerextension.openApiClient.model.ProviderRequest


import java.net.URI

class DatalandController() {

    val basepathToTrustee = "http://20.31.200.61"
    val defaultApi = DefaultApi(basepathToTrustee)

    fun registerDataAsAsset(data: String?): String {

        val assetResponse = defaultApi.registerAsset(
            ProviderRequest(
                participantId = "eurodat-connector-test",

                participantConnectorAddress = "localhost:9292/api/v1/ids/data",

                ownerId = "owner-ID",

                contentType = "persistent",

                asset = Asset(
                    mapOf(
                        "asset:prop:id" to "test-asset",
                        "endpoint" to "https://filesamples.com/samples/code/json/sample2.json"
                    )
                ),

                policy = Policy(
                    uid = "956e172f-2de1-4501-8881-057a57fd0e60",
                    permissions = listOf(Permission(target = "test-asset", action = Action(type = "USE")))
                ),


                provider = URI("urn:connector:provider"),

                consumer = URI("urn:connector:provider"),
            )
        )

        val assetId = assetResponse.asset?.properties?.get("asset:prop:id").toString()

        //["asset"]["properties"]["asset:prop:id"].asText()
        //val contractDefinitionId = assetResponse["contractDefinition"]["id"].asText()

        return assetId

    }

/*
    fun registerDataAsAsset(data: String?): String {

        val action = Action.Builder.newInstance()
            .type("USE")
            .build()
        val permission = Permission.Builder.newInstance()
            .target("test-asset")
            .action(action)
            .build()
        val asset = Asset.Builder.newInstance()
            .id("test-asset")
            .property("endpoint", "https://filesamples.com/samples/code/json/sample2.json")
            .build()
        val policy = Policy.Builder.newInstance()
            .id("956e172f-2de1-4501-8881-057a57fd0e60")
            .permission(permission)
            .build()

        val providerRequest = ProviderRequest(
            "eurodat-connector-test",
            "$providerIdsURL/api/v1/ids/data",
            "owner-ID",
            "persistent",
            asset,
            policy,
            URI("urn:connector:provider"),
            URI("urn:connector:consumer")
        )
        val providerRequestString = jsonMapper.writeValueAsString(providerRequest)

        val assetResponse = trusteeClient.post("/api/asset/register", providerRequestString)
        val assetId = assetResponse["asset"]["properties"]["asset:prop:id"].asText()
        val contractDefinitionId = assetResponse["contractDefinition"]["id"].asText()

        return assetId
    }*/
}