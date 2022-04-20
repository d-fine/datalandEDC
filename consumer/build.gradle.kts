/*
 * This content is based on copyrighted work as referenced below.
 *
 * Changes made:
 * - added repositories
 * - updated dependencies
 * - added dependencies
 * - changed build plugin from shadow to jib
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

plugins {
    `java-library`
    id("application")
    id("com.google.cloud.tools.jib") version "3.1.4"
}

val jupiterVersion: String by project
val rsApi: String by project
val connectorVersion = "0.0.1"

repositories {
    mavenCentral()
    maven("https://maven.iais.fraunhofer.de/artifactory/eis-ids-public")
}

dependencies {

    implementation("org.eclipse.dataspaceconnector:core")
    implementation("org.eclipse.dataspaceconnector:in-memory")
    implementation("org.eclipse.dataspaceconnector:http")
    //implementation("org.eclipse.dataspaceconnector:vault-fs:$connectorVersion")
    //implementation("org.eclipse.dataspaceconnector:oauth2-core:$connectorVersion")
    implementation("org.eclipse.dataspaceconnector:configuration-fs")
    implementation("org.eclipse.dataspaceconnector:iam-mock")
    implementation("org.eclipse.dataspaceconnector:control")
    implementation("org.eclipse.dataspaceconnector:ids")
    implementation("org.eclipse.dataspaceconnector:transfer-functions-core")
    implementation("org.eclipse.dataspaceconnector:data-plane-framework")
    implementation("org.eclipse.dataspaceconnector:data-plane-api")
    implementation("org.eclipse.dataspaceconnector:dataloading:$connectorVersion")
    implementation(project(":transfer-file"))
    implementation(project(":api"))
}

application {
    mainClass.set("org.eclipse.dataspaceconnector.boot.system.runtime.BaseRuntime")
    applicationDefaultJvmArgs = listOf("-Dedc.fs.config=config.properties", "-Dids.webhook.address=http://localhost:9191")
}
/*

jib {
    val tag = System.getenv("CI_PIPELINE_ID")
    val registry = System.getenv("ACR_INSTANCE")

    from {
        image = "openjdk:11-jre-slim-buster"
    }
    to {
        image = "$registry/eurodat/edc-consumer:$tag"
    }
    container {
        mainClass = "org.eclipse.dataspaceconnector.boot.system.runtime.BaseRuntime"
        jvmFlags = listOf("-Dedc.fs.config=app/config.properties", "-Dids.webhook.address=http://localhost:9191")
        ports = listOf("9191")
    }
}
*/
