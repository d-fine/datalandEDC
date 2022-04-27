/*
 * This content is based on copyrighted work as referenced below.
 *
 * Changes made:
 * - added repositories
 * - updated dependencies
 */

/*
 *  Copyright (c) 2020, 2021 Microsoft Corporation
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Microsoft Corporation - initial API and implementation
 *       Fraunhofer Institute for Software and Systems Engineering - added dependencies
 *
 */
val sonarSources by extra(sourceSets.asMap.values.flatMap { sourceSet -> sourceSet.allSource })
val jacocoSources by extra(sonarSources)
val jacocoClasses by extra(
    sourceSets.asMap.values.flatMap { sourceSet ->
        sourceSet.output.classesDirs.flatMap {
            fileTree(it).files
        }
    }
)
plugins {
    kotlin("jvm")
    kotlin("kapt")
    jacoco
    id("org.openapi.generator") version "5.4.0"
}

jacoco {
    toolVersion = "0.8.7"
}

tasks.test {
    useJUnitPlatform()

    extensions.configure(JacocoTaskExtension::class) {
        setDestinationFile(file("$buildDir/jacoco/jacoco.exec"))
    }
}
val rsApi: String by project

dependencies {
    implementation("org.eclipse.dataspaceconnector:spi")

    implementation("io.swagger.core.v3:swagger-annotations:2.2.0")
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    implementation("com.squareup.moshi:moshi-kotlin:1.13.0")
    implementation("org.junit.jupiter:junit-jupiter:5.8.2")
    api("jakarta.ws.rs:jakarta.ws.rs-api:$rsApi")

    implementation("org.eurodat.connector:api")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    dependsOn("generateEuroDatBrokerExtensionClient")
}

val euroDatOpenApiJson = "OpenApiEuroDat.json"
val taskName = "generateEuroDatBrokerExtensionClient"
val clientOutputDir = "$buildDir/Clients/brokerextension"
val apiSpecLocation = "$projectDir/$euroDatOpenApiJson"
val destinationPackage = "org.eurodat.brokerextension.openApiClient"

tasks.register(taskName, org.openapitools.generator.gradle.plugin.tasks.GenerateTask::class) {
    input = project.file(apiSpecLocation).path
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
}
sourceSets {
    val main by getting
    main.java.srcDir("$clientOutputDir/src/main/kotlin")
}
tasks.test {
    useJUnitPlatform()
/*
    extensions.configure(JacocoTaskExtension::class) {
        setDestinationFile(file("$buildDir/jacoco/jacoco.exec"))
    }*/
}

ktlint {
    filter {
        exclude("**/brokerextension/**")
    }
}
