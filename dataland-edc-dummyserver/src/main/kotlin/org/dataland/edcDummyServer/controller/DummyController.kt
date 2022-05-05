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

    override fun selectDataById(dataId: String): ResponseEntity<Map <String, String>> {
        return ResponseEntity.ok(mapOf("dataId" to dataStore.selectDataSet(dataId)))
    }

    override fun insertData(body: String?): ResponseEntity<Map<String, String>> {
        return ResponseEntity.ok(mapOf("dataId" to dataStore.insertDataSet(body ?: "")))
    }
}
