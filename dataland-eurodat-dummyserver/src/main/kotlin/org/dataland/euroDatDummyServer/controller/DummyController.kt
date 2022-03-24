package org.dataland.euroDatDummyServer.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.euroDatDummyServer.openApiServer.api.DefaultApi
import org.dataland.euroDatDummyServer.openApiServer.model.Asset
import org.dataland.euroDatDummyServer.openApiServer.model.AssetResponse
import org.dataland.euroDatDummyServer.openApiServer.model.ConsumerRequest
import org.dataland.euroDatDummyServer.openApiServer.model.ContractDefinition
import org.dataland.euroDatDummyServer.openApiServer.model.ProviderRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

/**
 * Implementation of API
 */
@RestController
class DummyController : DefaultApi {

    override fun prepareData(consumerRequest: ConsumerRequest?): ResponseEntity<Any> {
        val bodyString = "{\"text1\": \"text2\"}"
        val jsonNode = ObjectMapper().readTree(bodyString)
        return ResponseEntity.ok(jsonNode)
    }

    override fun registerAsset(providerRequest: ProviderRequest?): ResponseEntity<AssetResponse> {
        val asset = Asset(mapOf("text1" to "text2"))
        val contractDefinition = ContractDefinition()
        val assetResponse = AssetResponse(asset, contractDefinition)
        return ResponseEntity.ok(assetResponse)
    }
}
