package org.dataland.edcDummyServer.controller

import org.dataland.edcDummyServer.openApiServer.api.DefaultApi
import org.dataland.edcDummyServer.service.InMemoryDataStore
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController


@RestController
class DummyController : DefaultApi  {
    val dataStore = InMemoryDataStore()

    override fun selectDataById(dataId: String): ResponseEntity<String> {
        return ResponseEntity.ok(dataStore.selectDataSet(dataId))
    }

    override fun request(header: Any?, payload: String?): ResponseEntity<String> {
        return ResponseEntity.ok(dataStore.insertDataSet(payload ?: ""))
    }

}


