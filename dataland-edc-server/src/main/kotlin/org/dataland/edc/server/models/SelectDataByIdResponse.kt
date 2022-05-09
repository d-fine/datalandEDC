package org.dataland.edc.server.models

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * --- API model for Dataland-internal-EDC-API ---
 * Response model for the case that data inside an asset in EuroDaT is retrieved via the Dataland EDC
 * @param data the actual data inside the requested asset
 */
data class SelectDataByIdResponse(
    @field:JsonProperty(required = true) val data: String,
)
