plugins {
    `java-library`
    id("org.openapi.generator") version "5.4.0"
}

val openApiSpecConfig by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}

dependencies {
    openApiSpecConfig(project(mapOf("path" to ":dataland-connector", "configuration" to "openApiSpec")))
}

tasks.register<Copy>("getOpenApiSpec") {
    from(openApiSpecConfig)
    into("$buildDir")
}

val destinationPackage = "org.dataland.datalandbackend.edcClient"
val jsonFile = rootProject.extra["OpenApiSpec"]

tasks.register("generateEdcClient", org.openapitools.generator.gradle.plugin.tasks.GenerateTask::class) {
    input = "$buildDir/$jsonFile"
    outputDir.set("$buildDir/Clients")
    modelPackage.set("$destinationPackage.model")
    apiPackage.set("$destinationPackage.api")
    packageName.set(destinationPackage)
    generatorName.set("kotlin")
    configOptions.set(
        mapOf(
            "dateLibrary" to "java17",
            "useTags" to "true"
        )
    )
    dependsOn("getOpenApiSpec")
}
