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
    mavenCentral()
    maven("https://maven.iais.fraunhofer.de/artifactory/eis-ids-public")
}

dependencies {
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
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
    implementation("org.eclipse.dataspaceconnector:dataloading")
    implementation("org.eclipse.dataspaceconnector:spi")
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

tasks.register<Jar>("fatJar") {
    manifest {
        attributes["Main-Class"] = "org.eclipse.dataspaceconnector.boot.system.runtime.BaseRuntime"
    }
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    with(tasks.jar.get() as CopySpec)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
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
