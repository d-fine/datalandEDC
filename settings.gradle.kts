rootProject.name = "connector"
includeBuild("trustee-platform/services/broker")
include("dataland-edc-client")
include("dataland-edc-dummyserver")
include("dataland-edc-server")

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            library("springdoc-openapi-ui", "org.springdoc:springdoc-openapi-ui:1.6.8")

            library("junit-jupiter", "org.junit.jupiter:junit-jupiter:5.8.2")
            library("junit-jupiter-engine", "org.junit.jupiter:junit-jupiter-engine:5.8.2")
            library("junit-jupiter-api", "org.junit.jupiter:junit-jupiter-api:5.8.2")

            library("moshi-kotlin", "com.squareup.moshi:moshi-kotlin:1.13.0")
            library("moshi-adapters", "com.squareup.moshi:moshi-adapters:1.13.0")

            library("swagger-jaxrs2-jakarta", "io.swagger.core.v3:swagger-jaxrs2-jakarta:2.2.0")
            library("swagger-gradle-plugin", "io.swagger.core.v3:swagger-gradle-plugin:2.2.0")
            library("swagger-annotations", "io.swagger.core.v3:swagger-annotations:2.2.0")

            library("okhttp", "com.squareup.okhttp3:okhttp:4.9.3")
            library("rs-api", "jakarta.ws.rs:jakarta.ws.rs-api:3.1.0")

            library("awaitility", "org.awaitility:awaitility:4.2.0")
            library("awaitility-kotlin", "org.awaitility:awaitility-kotlin:4.2.0")

            library("edc-core", "org.eclipse.dataspaceconnector:core:0.0.1-SNAPSHOT")
            library("edc-assetindex-memory", "org.eclipse.dataspaceconnector:assetindex-memory:0.0.1-SNAPSHOT")
            library(
                "edc-transfer-process-store-memory",
                "org.eclipse.dataspaceconnector:transfer-process-store-memory:0.0.1-SNAPSHOT"
            )
            library(
                "edc-contractnegotiation-store-memory",
                "org.eclipse.dataspaceconnector:contractnegotiation-store-memory:0.0.1-SNAPSHOT"
            )
            library(
                "edc-contractdefinition-store-memory",
                "org.eclipse.dataspaceconnector:contractdefinition-store-memory:0.0.1-SNAPSHOT"
            )
            library("edc-http", "org.eclipse.dataspaceconnector:http:0.0.1-SNAPSHOT")
            library(
                "edc-filesystem-configuration",
                "org.eclipse.dataspaceconnector:filesystem-configuration:0.0.1-SNAPSHOT"
            )
            library("edc-filesystem-vault", "org.eclipse.dataspaceconnector:filesystem-vault:0.0.1-SNAPSHOT")
            library("edc-oauth2-core", "org.eclipse.dataspaceconnector:oauth2-core:0.0.1-SNAPSHOT")
            library("edc-control-api", "org.eclipse.dataspaceconnector:control-api:0.0.1-SNAPSHOT")
            library("edc-ids", "org.eclipse.dataspaceconnector:ids:0.0.1-SNAPSHOT")
            library("edc-dataloading", "org.eclipse.dataspaceconnector:dataloading:0.0.1-SNAPSHOT")
            library("edc-spi", "org.eclipse.dataspaceconnector:spi:0.0.1-SNAPSHOT")
        }
    }
}
