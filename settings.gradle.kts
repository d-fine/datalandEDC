rootProject.name = "connector"
include("dataland-edc-client")
include("dataland-edc-dummyserver")
include("dataland-edc-server")

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            library("springdoc-openapi-ui", "org.springdoc:springdoc-openapi-ui:1.6.12")

            library("junit-jupiter", "org.junit.jupiter:junit-jupiter:5.9.1")
            library("junit-jupiter-engine", "org.junit.jupiter:junit-jupiter-engine:5.9.0")
            library("junit-jupiter-api", "org.junit.jupiter:junit-jupiter-api:5.9.0")

            library("moshi-kotlin", "com.squareup.moshi:moshi-kotlin:1.14.0")
            library("moshi-adapters", "com.squareup.moshi:moshi-adapters:1.14.0")

            library("swagger-jaxrs2-jakarta", "io.swagger.core.v3:swagger-jaxrs2-jakarta:2.2.6")
            library("swagger-gradle-plugin", "io.swagger.core.v3:swagger-gradle-plugin:2.2.6")
            library("swagger-annotations", "io.swagger.core.v3:swagger-annotations:2.2.6")

            library("okhttp", "com.squareup.okhttp3:okhttp:4.10.0")
            library("rs-api", "jakarta.ws.rs:jakarta.ws.rs-api:3.1.0")
            library("jakarta-annotation-api", "jakarta.annotation:jakarta.annotation-api:2.1.1")

            library("awaitility", "org.awaitility:awaitility:4.2.0")
            library("awaitility-kotlin", "org.awaitility:awaitility-kotlin:4.2.0")

            library(
                "edc-filesystem-configuration",
                "org.eclipse.dataspaceconnector:filesystem-configuration:0.0.1-SNAPSHOT"
            )
            library("edc-filesystem-vault", "org.eclipse.dataspaceconnector:filesystem-vault:0.0.1-SNAPSHOT")
            library("edc-oauth2-core", "org.eclipse.dataspaceconnector:oauth2-core:0.0.1-SNAPSHOT")
            library("edc-ids", "org.eclipse.dataspaceconnector:ids:0.0.1-SNAPSHOT")
            library("edc-control-plane-core", "org.eclipse.dataspaceconnector:control-plane-core:0.0.1-SNAPSHOT")
        }
    }
}
