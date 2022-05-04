package org.dataland.edc.server.controller

import org.dataland.edc.server.service.DataManager
import org.eclipse.dataspaceconnector.dataloading.AssetLoader
import org.eclipse.dataspaceconnector.extensions.api.ConsumerApiController
import org.eclipse.dataspaceconnector.spi.contract.offer.store.ContractDefinitionStore
import org.eclipse.dataspaceconnector.spi.system.ServiceExtensionContext

class DatalandController(val context: ServiceExtensionContext) {

    private val dataManager = DataManager(context)

    fun uploadAssetToEuroDaT(data: String, assetLoader: AssetLoader, contractDefinitionStore: ContractDefinitionStore): String {
        return dataManager.uploadAssetToEuroDaT(data, assetLoader, contractDefinitionStore)
    }

    fun getProvidedAsset(providerAssetId: String): String {
        return dataManager.getProvidedAsset(providerAssetId)
    }

    fun getAssetFromEuroDaT(dataId: String, consumerApiController: ConsumerApiController): String {
        val splitDataId = dataId.split(":")
        if (splitDataId.size != 2) throw IllegalArgumentException("The data ID $dataId has an invalid format.")
        return dataManager.getAssetFromEuroDaT(
            assetId = splitDataId[0],
            contractDefinitionId = splitDataId[1]
        )
    }

    fun storeReceivedAsset(id: String, decodedData: String) {
        return dataManager.storeReceivedAsset(id, decodedData)
    }
}
