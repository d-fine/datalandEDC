package org.dataland.edc.server.extensions

import org.dataland.edc.server.service.DataManager
import org.dataland.edc.server.service.EurodatAssetCache
import org.dataland.edc.server.service.EurodatService
import org.dataland.edc.server.service.LocalAssetStore
import org.dataland.edc.server.service.ThreadAwareMonitor
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
    private lateinit var eurodatAssetCache: EurodatAssetCache

    @Inject
    private lateinit var eurodatService: EurodatService

    @Inject
    private lateinit var threadAwareMonitor: ThreadAwareMonitor

    /**
     * Returns the DataManager object - A central object used for requesting
     * and storing assets in EuroDaT
     */
    @Provider
    fun getDataManager(context: ServiceExtensionContext): DataManager {
        return DataManager(
            context,
            eurodatService,
            localAssetStore,
            eurodatAssetCache,
            threadAwareMonitor,
        )
    }
}
