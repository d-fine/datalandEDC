package org.dataland.edcDummyServer.service

import org.dataland.edcDummyServer.interfaces.DataStoreInterface
import org.springframework.stereotype.Component
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Simple implementation of a data store using in memory storage
 */
@Component
class InMemoryDataStore : DataStoreInterface {
    var data: ConcurrentHashMap<String, String> = ConcurrentHashMap()

    override fun insertDataSet(data: String): String {
        val assetId = UUID.randomUUID().toString()
        val contractDefinitionId = UUID.randomUUID().toString()
        val dataID = "$assetId:$contractDefinitionId"
        this.data[dataID] = data
        return dataID
    }

    override fun selectDataSet(dataId: String): String {
        return data[dataId] ?: ""
    }
}
