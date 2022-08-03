package org.dataland.edc.server.models

/**
 * In order to reference an asset in EuroDaT two variables are needed
 * the asset id and the contract definition/offer id. This class stores both
 * @param contractOfferId The EuroDaT contract offer id
 * @param eurodatAssetId The EuroDaT asset id
 */
data class EurodatAssetLocation(val contractOfferId: String, val eurodatAssetId: String)
