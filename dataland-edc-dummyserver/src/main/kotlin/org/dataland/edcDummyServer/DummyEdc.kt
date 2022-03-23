package org.dataland.edcDummyServer

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * Class for running the euroDaT dummy server
 */
@SpringBootApplication
class DummyEdc

/**
 * Main function to execute the spring boot service
 */
fun main(args: Array<String>) {
    @Suppress("SpreadOperator")
    runApplication<DummyEdc>(*args)
}
