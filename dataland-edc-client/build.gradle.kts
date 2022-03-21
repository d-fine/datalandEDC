/*
 * This content is based on copyrighted work as referenced below.
 *
 * Changes made:
 * - added repositories
 * - updated dependencies
 * - added dependencies
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

val swaggerJaxrs2Version: String by project
val rsApi: String by project

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

val taskName = "generateClient"
val clientOutputDir = "$buildDir/Clients"
val destinationPackage = "org.dataland.datalandbackend.edcClient"
val jsonOutputDir = buildDir
val jsonFile = rootProject.extra["OpenApiSpec"]


tasks.register(taskName, org.openapitools.generator.gradle.plugin.tasks.GenerateTask::class) {
    input = project.file("$jsonOutputDir/$jsonFile").path
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