package org.dataland.edc.server.exceptions

/**
 * Exception that can be thrown in case a Timeout with EuroDaT occurs
 */
class EuroDatTimeoutException(message: String, cause: Throwable) : Exception(message, cause)
