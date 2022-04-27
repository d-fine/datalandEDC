# Dependencies

To build the api extension, the OpenApi Clients for the EuroDaT Broker need to be built. To do so:
* run `./gradlew -p trustee-platform/services/broker broker-extension:resolve` to generate the OpenApi Specification
* run `cp trustee-platform/services/broker-extension/build/brokerOpenApi/OpenApiEuroDat.json api/build` to copy the OpenApi Specification to the api's build directory
* run `./gradlew api:generateEuroDatBrokerExtensionClient` to generate the clients
