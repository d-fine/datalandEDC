package org.dataland.edc.server.extensions

import org.dataland.edc.server.service.ThreadAwareMonitor
import org.eclipse.dataspaceconnector.spi.monitor.Monitor
import org.eclipse.dataspaceconnector.spi.system.Provider
import org.eclipse.dataspaceconnector.spi.system.ServiceExtension

/**
 * Creates a EuroDaTService Instance with the required injects
 * and makes it available to other components
 */
class ThreadAwareMonitorExtension : ServiceExtension {

    /**
     * Creates a EuroDaTService Instance with the required injects
     * and makes it available to other components
     */
    @Provider
    fun provideThreadAwareMonitor(): Monitor {
        return ThreadAwareMonitor()
    }
}
