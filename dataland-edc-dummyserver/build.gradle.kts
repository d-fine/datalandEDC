// server
val sonarSources by extra(sourceSets.asMap.values.flatMap { sourceSet -> sourceSet.allSource })
val jacocoSources by extra(sonarSources)
val jacocoClasses by extra(
    sourceSets.asMap.values.flatMap { sourceSet ->
        sourceSet.output.classesDirs.flatMap {
            fileTree(it).files
        }
    }
)
repositories {
    mavenCentral()
}

plugins {
    id("org.springframework.boot") version "2.6.4"
    kotlin("jvm")
    jacoco
    kotlin("plugin.spring") version "1.6.10"
    id("com.github.johnrengelman.processes") version "0.5.0"
    id("org.springdoc.openapi-gradle-plugin") version "1.3.3"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
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

java.sourceCompatibility = JavaVersion.VERSION_17

val openApiSpecConfig by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springdoc:springdoc-openapi-ui:1.6.6")
    implementation("org.junit.jupiter:junit-jupiter:5.8.2")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    openApiSpecConfig(project(mapOf("path" to ":dataland-connector", "configuration" to "openApiSpec")))
}

tasks.register<Copy>("getOpenApiSpec") {
    from(openApiSpecConfig)
    into("$buildDir")
}

val taskName = "generateEdcServer"
val serverOutputDir = "$buildDir/Server/edc"
val apiSpecLocation = "$buildDir/" + rootProject.extra["OpenApiSpec"]
val destinationPackage = "org.dataland.edcDummyServer.openApiServer"

tasks.register(taskName, org.openapitools.generator.gradle.plugin.tasks.GenerateTask::class) {
    input = apiSpecLocation
    outputDir.set(serverOutputDir)
    modelPackage.set("$destinationPackage.model")
    apiPackage.set("$destinationPackage.api")
    packageName.set(destinationPackage)
    generatorName.set("kotlin-spring")
    dependsOn("getOpenApiSpec")
    configOptions.set(
        mapOf(
            "dateLibrary" to "java17",
            "interfaceOnly" to "true",
            "useTags" to "true"
        )
    )
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    dependsOn(taskName)
}

sourceSets {
    val main by getting
    main.java.srcDir("$serverOutputDir/src/main/kotlin")
}

ktlint {
    filter {
        exclude("**/openApiServer/**")
    }
}
