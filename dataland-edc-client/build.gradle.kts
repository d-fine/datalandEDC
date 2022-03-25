val sonarSources by extra(sourceSets.asMap.values.flatMap { sourceSet -> sourceSet.allSource })

plugins {
    id("org.openapi.generator") version "5.4.0"
}

val openApiSpecConfig by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}

tasks.withType<Jar> {
    dependsOn("generateEdcClient")
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
val clientOutputDir = "$buildDir/Clients"

tasks.register("generateEdcClient", org.openapitools.generator.gradle.plugin.tasks.GenerateTask::class) {
    input = "$buildDir/$jsonFile"
    outputDir.set(clientOutputDir)
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

sourceSets {
    val main by getting
    main.java.srcDir("$clientOutputDir/src/main/kotlin")
}
