// server
val sonarSources by extra(emptyList<File>())
val jacocoSources by extra(emptyList<File>())
val jacocoClasses by extra(emptyList<File>())

plugins {
    id("org.springframework.boot") version "2.6.4"
    kotlin("jvm")
    kotlin("plugin.spring") version "1.6.10"
    id("com.github.johnrengelman.processes") version "0.5.0"
    id("org.springdoc.openapi-gradle-plugin") version "1.3.3"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    id("org.openapi.generator") version "5.4.0"
}

java.sourceCompatibility = JavaVersion.VERSION_17

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.eclipse.dataspaceconnector:dataloading")
    implementation("org.springdoc:springdoc-openapi-ui:1.6.6")
    implementation("org.junit.jupiter:junit-jupiter:5.8.2")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

val serverOutputDir = "$buildDir/Server/euroDat"
val destinationPackage = "org.dataland.euroDatDummyServer.openApiServer"
val taskName = "generateEuroDatServer"

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    dependsOn(taskName)
}

val spec = rootProject.extra["OpenApiSpecEuroDat"]

tasks.register(taskName, org.openapitools.generator.gradle.plugin.tasks.GenerateTask::class) {
    input = "$rootDir/resources/$spec"
    outputDir.set(serverOutputDir)
    modelPackage.set("$destinationPackage.model")
    apiPackage.set("$destinationPackage.api")
    packageName.set(destinationPackage)
    generatorName.set("kotlin-spring")
    configOptions.set(
        mapOf(
            "dateLibrary" to "java17",
            "interfaceOnly" to "true",
            "useTags" to "true"
        )
    )
}

sourceSets {
    val main by getting
    main.java.srcDir("$serverOutputDir/src/main/kotlin")
}

tasks.test {
    useJUnitPlatform()
}

ktlint {
    filter {
        exclude("**/openApiServer/**")
    }
}
