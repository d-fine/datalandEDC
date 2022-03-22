package org.dataland.edcDummyServer.service

import org.dataland.edcDummyServer.interfaces.DataStoreInterface
import org.springframework.stereotype.Component

/**
 * Simple implementation of a data store using in memory storage
 */
@Component
class InMemoryDataStore : DataStoreInterface {
    var data = mutableMapOf<String, String>()
    private var dataCounter = 0

    override fun insertDataSet(data: String): String {
        dataCounter++
        this.data["$dataCounter"] = data
        return "$dataCounter"
    }

    override fun selectDataSet(dataId: String): String {
        return data[dataId] ?: ""
    }
}
