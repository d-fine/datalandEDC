package org.dataland.edcDummyServer.controller

import org.dataland.edcDummyServer.openApiServer.api.DefaultApi
import org.dataland.edcDummyServer.service.InMemoryDataStore
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

/**
 * Implementation of API
 */
@RestController
class DummyController : DefaultApi {
    val dataStore = InMemoryDataStore()

    override fun selectDataById(assetId: String, contractDefinitionId: String): ResponseEntity<String> {
        return ResponseEntity.ok(dataStore.selectDataSet(assetId))
    }

    override fun insertData(body: String?): ResponseEntity<String> {
        return ResponseEntity.ok(dataStore.insertDataSet(body ?: ""))
    }
}
