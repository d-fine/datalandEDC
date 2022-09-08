package org.dataland.edc.server.service

import org.eclipse.dataspaceconnector.spi.monitor.Monitor

/**
 * A monitor that also tells the log what thread the message came from
 * @param monitor the actual monitor to use
 */
class ThreadAwareMonitor(private val monitor: Monitor) {
    private fun addThreadToMessage(message: String): String {
        return "${Thread.currentThread().name} - $message"
    }

    /**
     * Puts a message to the monitor with severity INFO
     * @param message the message to be monitored
     */
    fun info(message: String) {
        monitor.info(addThreadToMessage(message))
    }

    /**
     * Puts a message to the monitor with severity SEVERE
     * @param message the message to be monitored
     */
    fun severe(message: String) {
        monitor.severe(addThreadToMessage(message))
    }
}
