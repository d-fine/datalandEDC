val sonarSources by extra(sourceSets.asMap.values.flatMap { sourceSet -> sourceSet.allSource })
val jacocoSources by extra(sonarSources)
val jacocoClasses by extra(
    sourceSets.asMap.values.flatMap { sourceSet ->
        sourceSet.output.classesDirs.flatMap {
            fileTree(it).files
        }
    }
)
val jacocoVersion: String by project
val connectorVersion: String by project

plugins {
    `java-library`
    id("application")
    id("io.swagger.core.v3.swagger-gradle-plugin")
    kotlin("jvm")
    kotlin("kapt")
    jacoco
    id("com.github.johnrengelman.shadow")
}

jacoco {
    toolVersion = jacocoVersion
    applyTo(tasks.run.get())
}

tasks.test {
    useJUnitPlatform()

    extensions.configure(JacocoTaskExtension::class) {
        setDestinationFile(file("$buildDir/jacoco/jacoco.exec"))
    }
}

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://maven.iais.fraunhofer.de/artifactory/eis-ids-public")
}

dependencies {
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation(libs.edc.core)
    implementation(libs.edc.assetindex.memory)
    implementation(libs.edc.transfer.process.store.memory)
    implementation(libs.edc.contractnegotiation.store.memory)
    implementation(libs.edc.contractdefinition.store.memory)
    implementation(libs.edc.http)
    implementation(libs.edc.filesystem.configuration)
    implementation(libs.edc.filesystem.vault)
    implementation(libs.edc.oauth2.core)
    implementation(libs.edc.control)
    implementation(libs.edc.ids)
    implementation(libs.edc.dataloading)
    implementation(libs.edc.spi)
    implementation("org.eurodat.connector:transfer-file")
    implementation("org.eurodat.broker:broker-rest-model")
    implementation(libs.swagger.jaxrs2.jakarta)
    implementation(libs.rs.api)
    implementation(libs.awaitility)
    implementation(libs.awaitility.kotlin)

    implementation(libs.swagger.annotations)
    implementation(libs.junit.jupiter)
    implementation(libs.okhttp)
}

application {
    mainClass.set("org.eclipse.dataspaceconnector.boot.system.runtime.BaseRuntime")
    applicationDefaultJvmArgs = listOf(
        "-Dedc.fs.config=config.properties", "-Dedc.keystore=keystore.jks", "-Dedc.keystore.password=123456",
        "-Dedc.vault=vault.properties"
    )
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    mergeServiceFiles()
}

val jsonOutputDir = buildDir
val jsonFile = rootProject.extra["OpenApiSpec"]

buildscript {
    dependencies {
        classpath(libs.swagger.gradle.plugin)
    }
}

pluginManager.withPlugin("io.swagger.core.v3.swagger-gradle-plugin") {
    tasks.withType<io.swagger.v3.plugins.gradle.tasks.ResolveTask> {
        outputFileName = jsonFile.toString().substringBeforeLast('.')
        prettyPrint = true
        classpath = java.sourceSets["main"].runtimeClasspath
        buildClasspath = classpath
        resourcePackages = setOf("org.dataland.edc.server.api")
        outputDir = file(jsonOutputDir)
    }
    configurations {
        all {
            exclude(group = "com.fasterxml.jackson.jaxrs", module = "jackson-jaxrs-json-provider")
        }
    }
}

val openApiSpec by configurations.creating {
    isCanBeConsumed = true
    isCanBeResolved = false
}

artifacts {
    add("openApiSpec", project.file("$jsonOutputDir/$jsonFile")) {
        builtBy("resolve")
    }
}
