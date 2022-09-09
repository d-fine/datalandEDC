package org.dataland.edc.server.exceptions

class EuroDatTimeoutException : Exception {
    constructor(message: String, cause: Throwable) : super(message, cause)
}
