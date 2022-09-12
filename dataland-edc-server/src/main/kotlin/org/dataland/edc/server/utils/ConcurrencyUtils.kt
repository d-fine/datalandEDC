package org.dataland.edc.server.utils

import java.util.concurrent.Semaphore

/**
 * Object holding utility methods for concurrency stuff
 */
object ConcurrencyUtils {
    /**
     * returns a new semaphore with one permit, that is already acquired
     */
    fun getAcquiredSemaphore(): Semaphore {
        val semaphore = Semaphore(1)
        semaphore.acquire()
        return semaphore
    }
}