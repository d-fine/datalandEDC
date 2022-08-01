package org.dataland.edc.server.service

import java.util.concurrent.ConcurrentHashMap

/**
 * An in-memory store of assets provided to EuroDaT
 */
class LocalAssetStore {
    private val store : ConcurrentHashMap<String, String> = ConcurrentHashMap()

    /**
     * Retrieves an asset from the store
     * @param id the id of the asset
     */
    fun retrieveFromStore(id : String) : String? {
        return store[id]
    }

    /**
     * Inserts an asset into the store
     * @param id the id of the asset
     * @param asset the actual asset data
     */
    fun insertIntoStore(id : String, asset : String) {
        store[id] = asset
    }

    /**
     * Deletes an asset from the store
     * @param id the id of the asset
     */
    fun deleteFromStore(id : String) {
        store.remove(id)
    }
}
