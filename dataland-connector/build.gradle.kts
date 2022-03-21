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
    id("application")
    id("io.swagger.core.v3.swagger-gradle-plugin") version "2.1.13"
    id("org.openapi.generator") version "5.4.0"
    id("com.rameshkp.openapi-merger-gradle-plugin") version "1.0.4"
}

repositories {
    mavenCentral()
    maven("https://maven.iais.fraunhofer.de/artifactory/eis-ids-public")
}

dependencies {
    implementation("org.eclipse.dataspaceconnector:core")
    implementation("org.eclipse.dataspaceconnector:in-memory:assetindex-memory")
    implementation("org.eclipse.dataspaceconnector:in-memory:transfer-store-memory")
    implementation("org.eclipse.dataspaceconnector:in-memory:negotiation-store-memory")
    implementation("org.eclipse.dataspaceconnector:in-memory:contractdefinition-store-memory")
    implementation("org.eclipse.dataspaceconnector:http")
    implementation("org.eclipse.dataspaceconnector:configuration-fs")
    implementation("org.eclipse.dataspaceconnector:iam-mock")
    implementation("org.eclipse.dataspaceconnector:control")
    implementation("org.eclipse.dataspaceconnector:ids")
    implementation(project(":api"))
}

application {
    mainClass.set("org.eclipse.dataspaceconnector.boot.system.runtime.BaseRuntime")
}

val taskName = "generateClient"
val clientOutputDir = "$buildDir/Clients"
val destinationPackage = "org.dataland.datalandbackend.edcClient"
val jsonOutputDir = buildDir
val jsonFile = "OpenApiSpec"

buildscript {
    dependencies {
        classpath("io.swagger.core.v3:swagger-gradle-plugin:2.1.13")
    }
}

pluginManager.withPlugin("io.swagger.core.v3.swagger-gradle-plugin") {

    dependencies {
        // this is used to scan the classpath and generate an openapi yaml file
        implementation("io.swagger.core.v3:swagger-jaxrs2-jakarta:${swaggerJaxrs2Version}")
        implementation("jakarta.ws.rs:jakarta.ws.rs-api:${rsApi}")
    }
// this is used to scan the classpath and generate an openapi yaml file
    tasks.withType<io.swagger.v3.plugins.gradle.tasks.ResolveTask> {
        outputFileName = jsonFile
        prettyPrint = true
        classpath = java.sourceSets["main"].runtimeClasspath
        buildClasspath = classpath
        resourcePackages = setOf("org.eclipse.dataspaceconnector")
        outputDir = file(jsonOutputDir)
    }
    configurations {
        all {
            exclude(group = "com.fasterxml.jackson.jaxrs", module = "jackson-jaxrs-json-provider")
        }
    }
}


tasks.register(taskName, org.openapitools.generator.gradle.plugin.tasks.GenerateTask::class) {
    input = project.file("$jsonOutputDir/$jsonFile.json").path
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
    dependsOn("resolve")
}
