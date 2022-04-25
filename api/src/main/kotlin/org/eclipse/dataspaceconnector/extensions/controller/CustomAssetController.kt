package org.eclipse.dataspaceconnector.extensions.controller

import org.eclipse.dataspaceconnector.policy.model.Action
import org.eclipse.dataspaceconnector.policy.model.Permission
import org.eclipse.dataspaceconnector.policy.model.Policy
import org.eclipse.dataspaceconnector.spi.contract.negotiation.ConsumerContractNegotiationManager
import org.eclipse.dataspaceconnector.spi.contract.negotiation.store.ContractNegotiationStore
import org.eclipse.dataspaceconnector.spi.contract.offer.store.ContractDefinitionStore
import org.eclipse.dataspaceconnector.spi.monitor.ConsoleMonitor
import org.eclipse.dataspaceconnector.spi.types.domain.asset.Asset
import org.eurodat.broker.controller.AssetController
import java.io.File
import java.net.URI

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




    //____________________________________________Asset Controller benutzen
    fun registerAsset(data: String): String{
        //val endpoint = buildDynamicEndpoint(buildJson(data))
        val providerRequest: ProviderRequest = ProviderRequest() //bauen

        val assetResponse = assetController.registerAsset(providerRequest)

        val dataId: String = /* aus der assetResponse muss jetzt quasi eine ID o.Ä. herausgelesen werden, unter der die Daten
        bei EuroDaT liegen*/

        return dataId
    }



    //___________________________________________ Auf Basis von E2E-Test:

    private val jsonMapper = jacksonObjectMapper()

    private val testCredentials = "password"
    private val trusteeClient = HttpClient(
        DefaultOkHttpClientFactoryImpl.create(false), trusteeURL, "APIKey", testCredentials)
    private val consumerClient = HttpClient(
        DefaultOkHttpClientFactoryImpl.create(false), consumerURL, "APIKey", testCredentials)

    private val action = Action.Builder.newInstance()
        .type("USE")
        .build()
    private val permission = Permission.Builder.newInstance()
        .target("test-asset")
        .action(action)
        .build()
    private val asset = Asset.Builder.newInstance()
        .id("test-asset")
        .property("endpoint", "https://filesamples.com/samples/code/json/sample2.json")
        .build()
    private val policy = Policy.Builder.newInstance()
        .id("956e172f-2de1-4501-8881-057a57fd0e60")
        .permission(permission)
        .build()
    init {
        trusteeClient.connectionCheck("/api/ids/description")
        consumerClient.connectionCheck("/api/health")
    }

    fun registerAsset2(data: String): String{
        // Provider registers asset with Trustee
        val providerRequest = ProviderRequest(
            "eurodat-connector-test",
            "$providerIdsURL/api/v1/ids/data",
            "owner-ID",
            "persistent",
            asset,
            policy,
            URI("urn:connector:provider"),
            URI("urn:connector:consumer")
        )
        val providerRequestString = jsonMapper.writeValueAsString(providerRequest)

        val assetResponse = trusteeClient.post("/api/asset/register", providerRequestString)
        val assetId = assetResponse["asset"]["properties"]["asset:prop:id"].asText()
        val contractDefinitionId = assetResponse["contractDefinition"]["id"].asText()

        return assetId
    }
}