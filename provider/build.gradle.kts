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
    implementation("org.eclipse.dataspaceconnector:in-memory:assetindex-memory")
    implementation("org.eclipse.dataspaceconnector:in-memory:transfer-store-memory")
    implementation("org.eclipse.dataspaceconnector:in-memory:negotiation-store-memory")
    implementation("org.eclipse.dataspaceconnector:in-memory:contractdefinition-store-memory")
    implementation("org.eclipse.dataspaceconnector:http")
    implementation("org.eclipse.dataspaceconnector:configuration-fs")
    implementation("org.eclipse.dataspaceconnector:iam-mock") //added manually by me (Emanuel)
    //implementation("org.eclipse.dataspaceconnector:vault-fs:$connectorVersion")
    //implementation("org.eclipse.dataspaceconnector:oauth2-core:$connectorVersion")
    implementation("org.eclipse.dataspaceconnector:control")
    implementation("org.eclipse.dataspaceconnector:ids")
    implementation(project(":transfer-file"))
    implementation(project(":api"))
}

application {
    mainClass.set("org.eclipse.dataspaceconnector.boot.system.runtime.BaseRuntime")
    applicationDefaultJvmArgs = listOf("-Dedc.fs.config=config.properties")
}

/*
jib {
    var tag = System.getenv("CI_PIPELINE_ID")
    var registry = System.getenv("ACR_INSTANCE")

    from {
        image = "openjdk:11-jre-slim-buster"
    }
    to {
        image = "$registry/eurodat/edc-provider:$tag"
    }
    container {
        mainClass = "org.eclipse.dataspaceconnector.boot.system.runtime.BaseRuntime"
        jvmFlags = listOf("-Dedc.fs.config=app/config.properties")
        ports = listOf("8181")
    }
}*/
