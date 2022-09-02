package org.dataland.edc.server.service

import org.dataland.edc.server.models.EurodatAssetLocation
import java.util.concurrent.ConcurrentHashMap

/**
 * An in-memory store of assets provided to EuroDaT
 */
class LocalAssetStore {
    private val dataStore: ConcurrentHashMap<String, String> = ConcurrentHashMap()
    private val idStore: ConcurrentHashMap<String, EurodatAssetLocation> = ConcurrentHashMap()

    /**
     * Retrieves an asset from the store
     * @param id the id of the asset
     */
    fun retrieveDataFromStore(id: String): String? {
        return dataStore[id]
    }

    /**
     * Inserts an asset into the store
     * @param id the id of the asset
     * @param asset the actual asset data
     */
    fun insertDataIntoStore(id: String, asset: String) {
        dataStore[id] = asset
    }

    /**
     * Deletes an asset from the store
     * @param id the id of the asset
     */
    fun deleteFromStore(id: String) {
        dataStore.remove(id)
        idStore.remove(id)
    }

    /**
     * Retrieves the EuroDat identifiers corresponding to the Dataland asset id
     * @param id the id of the asset
     */
    fun retrieveEurodatAssetLocationFromStore(id: String): EurodatAssetLocation? {
        return idStore[id]
    }

    /**
     * Inserts the EuroDat identifiers corresponding to the Dataland asset id
     * @param id the id of the asset
     * @param location the identifiers of the asset in EuroDat
     */
    fun insertEurodatAssetLocationIntoStore(id: String, location: EurodatAssetLocation) {
        idStore[id] = location
    }
}
