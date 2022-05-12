package org.dataland.edc.server.models

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * --- API model for Dataland-internal-EDC-API ---
 * Response model for the case that the health endpoint of Dataland EDC receives a request
 * @param response is the response that the sender of the request to the health endpoint receives
 */
data class CheckHealthResponse(
    @field:JsonProperty(required = true) val response: String,
)
