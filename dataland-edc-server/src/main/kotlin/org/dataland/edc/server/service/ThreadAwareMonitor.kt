package org.dataland.edc.server.service

import org.eclipse.dataspaceconnector.spi.monitor.ConsoleMonitor

/**
 * A monitor that also tells the log what thread the message came from
 */
class ThreadAwareMonitor : ConsoleMonitor() {
    private fun addThreadToMessage(message: String): String {
        return "${Thread.currentThread().name} - $message"
    }

    /**
     * Puts a message to the monitor with severity INFO
     * @param message the message to be monitored
     */
    override fun info(message: String, vararg errors: Throwable?) {
        super.info(addThreadToMessage(message), *errors)
    }

    /**
     * Puts a message to the monitor with severity SEVERE
     * @param message the message to be monitored
     */
    override fun severe(message: String, vararg errors: Throwable?) {
        super.severe(addThreadToMessage(message), *errors)
    }
}
