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
    var storedData: ConcurrentHashMap<String, String> = ConcurrentHashMap()

    override fun insertDataSet(data: String): String {
        val trusteeAssetId = UUID.randomUUID().toString()
        val contractDefinitionId = UUID.randomUUID().toString()
        val dataID = "$trusteeAssetId:$contractDefinitionId"
        storedData[dataID] = data
        return dataID
    }

    override fun selectDataSet(dataId: String): String {
        return storedData[dataId] ?: ""
    }
}
