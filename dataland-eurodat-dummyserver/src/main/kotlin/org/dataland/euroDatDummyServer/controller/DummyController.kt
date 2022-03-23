package org.dataland.euroDatDummyServer.controller

import org.dataland.euroDatDummyServer.openApiServer.api.DefaultApi
import org.dataland.euroDatDummyServer.openApiServer.model.ConsumerRequest
import org.dataland.euroDatDummyServer.openApiServer.model.ProviderRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class DummyController : DefaultApi {

    override fun prepareData(consumerRequest: ConsumerRequest?): ResponseEntity<Unit> {
        return ResponseEntity.ok(Unit)
    }

    override fun registerAsset(providerRequest: ProviderRequest?): ResponseEntity<Unit> {
        return ResponseEntity.ok(Unit)
    }
}
