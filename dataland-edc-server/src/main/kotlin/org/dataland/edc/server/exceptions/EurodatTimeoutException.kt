package org.dataland.edc.server.exceptions

/**
 * Extension of exceptions. Adds the timeout error exception.
 */
class EurodatTimeoutException(override val message: String?) : Exception(message)
