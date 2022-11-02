package org.dataland.edc.server.extensions

import org.dataland.edc.server.service.EurodatAssetCache
import org.dataland.edc.server.service.LocalAssetStore
import org.eclipse.dataspaceconnector.runtime.metamodel.annotation.Provider
import org.eclipse.dataspaceconnector.spi.system.ServiceExtension

/**
 * An Extension that provides the LocalAssetStore and EuroDaTAssetcache
 * to other extensions
 */
class DatalandAssetStoreExtension : ServiceExtension {
    /**
     * Provides an In-Memory store for storing
     * assets that are to be retrieved by EuroDaT
     */
    @Provider
    fun getLocalAssetStore(): LocalAssetStore {
        return LocalAssetStore()
    }

    /**
     * Provides an In-Memory cache for EuroDaT assets
     * to make subsequent requests for the same asset significantly faster
     */
    @Provider
    fun getEurodatAssetCache(): EurodatAssetCache {
        return EurodatAssetCache()
    }
}
