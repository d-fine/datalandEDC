package org.eclipse.dataspaceconnector.extensions.controller

import org.eclipse.dataspaceconnector.extensions.models.DALADefaultOkHttpClientFactoryImpl
import org.eclipse.dataspaceconnector.extensions.models.DALAHttpClient
import org.eclipse.dataspaceconnector.policy.model.Action
import org.eclipse.dataspaceconnector.policy.model.Permission
import org.eclipse.dataspaceconnector.policy.model.Policy
import org.eclipse.dataspaceconnector.spi.types.domain.asset.Asset
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.eurodat.broker.model.ProviderRequest



import java.net.URI


class DatalandController() {

    private val jsonMapper = jacksonObjectMapper()

    private val trusteeURL = "http://20.31.200.61:80/api"
    private val providerIdsURL = "http://dataland-tunnel.duckdns.org:9292"


    //private val consumerIdsURL = providerIdsURL

    val testCredentials = "password"

    private val trusteeClient = DALAHttpClient(
        DALADefaultOkHttpClientFactoryImpl.create(false), trusteeURL, "APIKey", testCredentials
    )
    /*private val consumerClient = DALAHttpClient(
        DALADefaultOkHttpClientFactoryImpl.create(false), consumerURL, "APIKey", testCredentials)*/

    private val action = Action.Builder.newInstance()
        .type("USE")
        .build()
    private val permission = Permission.Builder.newInstance()
        .target("test-asset")
        .action(action)
        .build()
    private val asset = Asset.Builder.newInstance()
        .id("test-asset")
        .property("endpoint", "https://filesamples.com/samples/code/json/sample2.json")
        .build()
    private val policy = Policy.Builder.newInstance()
        .id("956e172f-2de1-4501-8881-057a57fd0e60")
        .permission(permission)
        .build()

    fun registerDataAsAsset(data: String?): String {

        // Provider registers asset with Trustee
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
    }
}
