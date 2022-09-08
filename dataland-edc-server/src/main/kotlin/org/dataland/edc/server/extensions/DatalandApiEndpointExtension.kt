package org.dataland.edc.server.extensions

import org.dataland.edc.server.controller.DatalandEurodatController
import org.dataland.edc.server.controller.DatalandInternalEdcController
import org.dataland.edc.server.service.DataManager
import org.dataland.edc.server.service.EurodatAssetCache
import org.dataland.edc.server.service.LocalAssetStore
import org.dataland.edc.server.service.ThreadAwareMonitor
import org.eclipse.dataspaceconnector.spi.WebService
import org.eclipse.dataspaceconnector.spi.system.Inject
import org.eclipse.dataspaceconnector.spi.system.ServiceExtension
import org.eclipse.dataspaceconnector.spi.system.ServiceExtensionContext

/**
 * Extends the EDC with the functionality required for Dataland
 */
class DatalandApiEndpointExtension : ServiceExtension {

    @Inject
    private lateinit var webService: WebService

    @Inject
    private lateinit var localAssetStore: LocalAssetStore

    @Inject
    private lateinit var eurodatAssetCache: EurodatAssetCache

    @Inject
    private lateinit var dataManager: DataManager

    @Inject
    private lateinit var threadAwareMonitor: ThreadAwareMonitor

    override fun name(): String {
        return "API Endpoint"
    }

    override fun initialize(context: ServiceExtensionContext) {
        webService.registerResource(DatalandInternalEdcController(dataManager, context, eurodatAssetCache, threadAwareMonitor))
        webService.registerResource(DatalandEurodatController(context, localAssetStore, eurodatAssetCache, threadAwareMonitor))
    }
}
