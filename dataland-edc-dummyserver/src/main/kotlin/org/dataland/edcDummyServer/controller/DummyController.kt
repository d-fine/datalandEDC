package org.dataland.edcDummyServer.controller

import io.swagger.v3.oas.annotations.Parameter
import org.dataland.edcDummyServer.openApiServer.api.DefaultApi
import org.dataland.edcDummyServer.openApiServer.model.CheckHealthResponse
import org.dataland.edcDummyServer.openApiServer.model.InsertDataResponse
import org.dataland.edcDummyServer.service.InMemoryDataStore
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

/**
 * Implementation of API
 */
@RestController
class DummyController : DefaultApi {
    val dataStore = InMemoryDataStore()

    override fun selectDataById(
        @Parameter(
            description = "",
            required = true
        ) @PathVariable(value = "dataId") dataId: String,
        @Parameter(description = "") @Valid @RequestParam(
            required = false,
            value = "correlationId"
        ) correlationId: String?
    ): ResponseEntity<String> {
        return ResponseEntity.ok(dataStore.selectDataSet(dataId))
    }

    override fun insertData(
        @Parameter(description = "") @Valid @RequestParam(
            required = false,
            value = "correlationId"
        ) correlationId: String?, @Parameter(description = "") @Valid @RequestBody(required = false) body: String?
    ): ResponseEntity<InsertDataResponse> {
        return ResponseEntity.ok(InsertDataResponse(dataStore.insertDataSet(body ?: "")))
    }

    override fun checkHealth(): ResponseEntity<CheckHealthResponse> {
        return ResponseEntity.ok(CheckHealthResponse("I am alive!"))
    }
}
