package org.eclipse.dataspaceconnector.extensions.controller

import org.eclipse.dataspaceconnector.extensions.api.ConsumerApiController
import org.eclipse.dataspaceconnector.extensions.service.DataManager

class DatalandController() {

    private val dataManager = DataManager()

    fun uploadAssetToEuroDaT(data: String): String {
        return dataManager.uploadAssetToEuroDaT(data)
    }

    fun getProvidedAsset(providerAssetId: String): String {
        return dataManager.getProvidedAsset(providerAssetId)
    }

    fun getAssetFromEuroDaT(
        dataId: String,
        consumerApiController: ConsumerApiController
    ): String {
        val splitDataId = dataId.split(":")
        if (splitDataId.size != 2) throw IllegalArgumentException("The data ID $dataId has an invalid format.")
        return dataManager.getAssetFromEuroDaT(
            assetId = splitDataId[0],
            contractDefinitionId = splitDataId[1],
            consumerApiController = consumerApiController
        )
    }

    fun storeReceivedAsset(id: String, decodedData: String) {
        return dataManager.storeReceivedAsset(id, decodedData)
    }
}
