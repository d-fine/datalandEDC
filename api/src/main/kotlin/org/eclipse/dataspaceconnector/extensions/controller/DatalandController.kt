package org.eclipse.dataspaceconnector.extensions.controller

import org.eurodat.brokerextension.openApiClient.api.DefaultApi
import org.eurodat.brokerextension.openApiClient.model.Action
import org.eurodat.brokerextension.openApiClient.model.Asset
import org.eurodat.brokerextension.openApiClient.model.Permission
import org.eurodat.brokerextension.openApiClient.model.Policy
import org.eurodat.brokerextension.openApiClient.model.ProviderRequest
import java.net.URI

class DatalandController() {

    val basepathToTrustee = "http://20.31.200.61:80/api"
    val defaultApi = DefaultApi(basepathToTrustee)

    fun registerDataAsAsset(data: String?): String {

        val assetResponse = defaultApi.registerAsset(
            ProviderRequest(
                participantId = "dataland-test",

                participantConnectorAddress = "http://emanuel-dfine.duckdns.org:9292/api/v1/ids",

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

        // ["asset"]["properties"]["asset:prop:id"].asText()
        // val contractDefinitionId = assetResponse["contractDefinition"]["id"].asText()

        return assetId
    }
}
