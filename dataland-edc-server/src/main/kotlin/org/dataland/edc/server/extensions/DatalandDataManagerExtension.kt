package org.dataland.edc.server.extensions

import org.dataland.edc.server.service.DataManager
import org.dataland.edc.server.service.EuroDaTAssetCache
import org.dataland.edc.server.service.EuroDaTService
import org.dataland.edc.server.service.LocalAssetStore
import org.eclipse.dataspaceconnector.spi.system.Inject
import org.eclipse.dataspaceconnector.spi.system.Provider
import org.eclipse.dataspaceconnector.spi.system.ServiceExtension
import org.eclipse.dataspaceconnector.spi.system.ServiceExtensionContext

/**
 * Provides the DataManager object - A central object used for requesting
 * and storing assets in EuroDaT
 */
class DatalandDataManagerExtension : ServiceExtension {

    @Inject
    private lateinit var localAssetStore: LocalAssetStore

    @Inject
    private lateinit var euroDaTAssetCache: EuroDaTAssetCache

    @Inject
    private lateinit var euroDaTService: EuroDaTService

    /**
     * Returns the DataManager object - A central object used for requesting
     * and storing assets in EuroDaT
     */
    @Provider
    fun getDataManager(context: ServiceExtensionContext): DataManager {
        return DataManager(
            context,
            euroDaTService,
            localAssetStore,
            euroDaTAssetCache
        )
    }
}
