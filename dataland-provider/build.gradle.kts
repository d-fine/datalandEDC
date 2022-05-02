/*
 * This content is based on copyrighted work as referenced below.
 *
 * Changes made:
 * - changed build plugin from shadow to jib
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
val sonarSources by extra(emptyList<File>())
val jacocoSources by extra(emptyList<File>())
val jacocoClasses by extra(emptyList<File>())

plugins {
    `java-library`
    id("application")
    id("io.swagger.core.v3.swagger-gradle-plugin") version "2.1.13"
}

val connectorVersion: String by project

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
    implementation("org.eclipse.dataspaceconnector:vault-fs:$connectorVersion")
    implementation("org.eclipse.dataspaceconnector:oauth2-core:$connectorVersion")
    implementation("org.eclipse.dataspaceconnector:control")
    implementation("org.eclipse.dataspaceconnector:ids")
    implementation("org.eurodat.connector:api")
    implementation("org.eurodat.connector:transfer-file")
    implementation(project(":api"))
}

application {
    mainClass.set("org.eclipse.dataspaceconnector.boot.system.runtime.BaseRuntime")
    applicationDefaultJvmArgs = listOf(
        "-Dedc.fs.config=config.properties", "-Dedc.keystore=keystore.jks", "-Dedc.keystore.password=123456",
        "-Dedc.vault=vault.properties"
    )
}
