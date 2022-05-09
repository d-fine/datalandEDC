package org.dataland.edcDummyServer.controller

import org.dataland.edcDummyServer.openApiServer.api.DefaultApi
import org.dataland.edcDummyServer.openApiServer.model.InsertDataResponse
import org.dataland.edcDummyServer.openApiServer.model.SelectDataByIdResponse
import org.dataland.edcDummyServer.service.InMemoryDataStore
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

/**
 * Implementation of API
 */
@RestController
class DummyController : DefaultApi {
    val dataStore = InMemoryDataStore()

    override fun selectDataById(dataId: String): ResponseEntity<SelectDataByIdResponse> {
        return ResponseEntity.ok(SelectDataByIdResponse(dataStore.selectDataSet(dataId)))
    }

    override fun insertData(body: String?): ResponseEntity<InsertDataResponse> {
        return ResponseEntity.ok(InsertDataResponse(dataStore.insertDataSet(body ?: "")))
    }
}
