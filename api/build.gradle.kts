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
    `java-library`
    id("application")
    jacoco
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

repositories {
    mavenCentral()
    maven("https://maven.iais.fraunhofer.de/artifactory/eis-ids-public")
}

dependencies {
    implementation("org.eclipse.dataspaceconnector:util")
    implementation("org.eclipse.dataspaceconnector:spi")
    implementation("io.swagger.core.v3:swagger-annotations:2.1.13")
    testImplementation("org.mockito:mockito-junit-jupiter:3.12.4")
    implementation("org.junit.jupiter:junit-jupiter:5.8.2")
    api("jakarta.ws.rs:jakarta.ws.rs-api:$rsApi")
}
