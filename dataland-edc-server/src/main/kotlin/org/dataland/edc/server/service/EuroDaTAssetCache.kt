package org.dataland.edc.server.service

import java.util.concurrent.ConcurrentHashMap

/**
 * A cache holding retrieved assets from EuroDaT
 * to make duplicate requests considerably faster
 */
class EuroDaTAssetCache {
    private val cache : ConcurrentHashMap<String, String> = ConcurrentHashMap()
    private val expectedAssetIds : MutableSet<String> = ConcurrentHashMap.newKeySet()

    /**
     * Retrieves an Asset from the Cache
     * @param id The asset id
     */
    fun retrieveFromCache(id : String) : String? {
        return cache[id]
    }

    /**
     * Inserts an Asset into the cache. The asset is only inserted
     * when the asset is expected. This is to prevent malicious third parties
     * from filling this cache with junk
     * @param id the asset id
     * @param asset the actual asset
     */
    fun insertIntoCache(id : String, asset : String) {
        if (expectedAssetIds.contains(id)) {
            cache[id] = asset
        }
    }


    /**
     * Returns whether an asset is expected or not
     * @param id the asset id
     */
    fun isAssetExpected(id : String) : Boolean {
        return expectedAssetIds.contains(id)
    }

    /**
     * Marks an asset as expected and evicts the cache for that asset id
     * @param id the asset id
     */
    fun expectAsset(id : String) {
        expectedAssetIds.add(id)
        cache.remove(id)
    }
}
