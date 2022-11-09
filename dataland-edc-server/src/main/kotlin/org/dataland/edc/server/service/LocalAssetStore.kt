package org.dataland.edc.server.service

import org.dataland.edc.server.models.AssetProvisionContainer
import java.util.concurrent.ConcurrentHashMap

/**
 * An in-memory store of assets provided to EuroDaT
 */
class LocalAssetStore {
    private val dataStore: ConcurrentHashMap<String, AssetProvisionContainer> = ConcurrentHashMap()

    /**
     * Retrieves an asset from the store
     * @param id the id of the asset
     */
    fun retrieveDataFromStore(id: String): AssetProvisionContainer? {
        return dataStore[id]
    }

    /**
     * Inserts an asset into the store
     * @param id the id of the asset
     */
    fun insertDataIntoStore(id: String, assetProvisionContainer: AssetProvisionContainer) {
        dataStore[id] = assetProvisionContainer
    }

    /**
     * Deletes an asset from the store
     * @param id the id of the asset
     */
    fun deleteFromStore(id: String) {
        dataStore.remove(id)
    }
}
