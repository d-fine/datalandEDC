package org.dataland.edc.server.models

import java.util.concurrent.Semaphore

/**
 * A class that holds the information necessary for a Provision process to EuroDaT
 */
data class AssetProvisionContainer(
    val data: String,
    var eurodatAssetLocation: EurodatAssetLocation?,
    var semaphore: Semaphore
)
