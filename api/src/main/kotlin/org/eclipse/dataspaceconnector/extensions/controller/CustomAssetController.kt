package org.eclipse.dataspaceconnector.extensions.controller

import org.eclipse.dataspaceconnector.spi.contract.negotiation.ConsumerContractNegotiationManager
import org.eclipse.dataspaceconnector.spi.contract.negotiation.store.ContractNegotiationStore
import org.eclipse.dataspaceconnector.spi.contract.offer.store.ContractDefinitionStore
import org.eclipse.dataspaceconnector.spi.monitor.ConsoleMonitor
import org.eclipse.dataspaceconnector.util.*
import org.eclipse.dataspaceconnector.test.*
import org.eurodat.broker.controller.AssetController
import java.io.File

class CustomAssetController {

    //Asset Controller Inputs
    private val testHost = System.getenv("NEO4J_INT_DB") ?: "localhost"
    private val neo4jManager = Neo4jManagerImpl(testHost, "neo4j", "secret")
    private val assetIndex = Neo4jAssetIndex(neo4jManager)
    private val assetDataRepository = mockk<AssetDataRepository>()
    private val monitor = ConsoleMonitor()
    private val assetRepository = AssetRepositoryImpl(assetIndex, assetIndex, assetDataRepository, monitor)
    private val consumerContractNegotiationManager = mockk<ConsumerContractNegotiationManager>()
    private val contractNegotiationStore = mockk<ContractNegotiationStore>()
    private val contractDefinitionStore = mockk<ContractDefinitionStore>()
    //Asset Controller bauen
    private val assetController = AssetController(
        assetRepository, consumerContractNegotiationManager,
        contractNegotiationStore, contractDefinitionStore
    )


    private fun buildJson(data: String): File {
        //baue json File aus dem data String => dies ist somit das Asset, das registered werden soll
        val file: File = File()// das Json File mit den Daten
        return file
    }

    private fun buildDynamicEndpoint(file: File): String {
        //packe das json File (Asset) hinter einen dynamischen Link => nutze diesen Link später beim registrieren des Assets
        val endpoint = "www.blabla.de/euTaxonomy1.json"//buildUrl(linkName = file.toString())
        return endpoint
    }

    //Asset Controller benutzen
    fun registerAsset(data: String): String{
        //val endpoint = buildDynamicEndpoint(buildJson(data))
        val providerRequest: ProviderRequest = ProviderRequest() //bauen

        val assetResponse = assetController.registerAsset(providerRequest)

        val dataId: String = /* aus der assetResponse muss jetzt quasi eine ID o.Ä. herausgelesen werden, unter der die Daten
        bei EuroDaT liegen*/

        return dataId
    }
}