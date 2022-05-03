val sonarSources by extra(emptyList<File>())
val jacocoSources by extra(emptyList<File>())
val jacocoClasses by extra(emptyList<File>())

plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    kotlin("kapt")
    id("org.openapi.generator") version "5.4.0"
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

val openApiSpecConfig by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}

java.sourceCompatibility = JavaVersion.VERSION_17

dependencies {
    implementation("org.springdoc:springdoc-openapi-ui:1.6.8")
    implementation("org.springframework:spring-context")
    implementation("org.springframework.boot:spring-boot")
    implementation("com.fasterxml.jackson.core:jackson-annotations")
    implementation("com.squareup.moshi:moshi-kotlin:1.13.0")
    implementation("com.squareup.moshi:moshi-adapters:1.13.0")
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    kapt("org.springframework.boot:spring-boot-configuration-processor")
    openApiSpecConfig(project(mapOf("path" to ":dataland-edc-server", "configuration" to "openApiSpec")))
}

tasks.register<Copy>("getOpenApiSpec") {
    from(openApiSpecConfig)
    into("$buildDir")
}

val taskName = "generateEdcClient"
val destinationPackage = "org.dataland.datalandbackend.edcClient"
val jsonFile = rootProject.extra["OpenApiSpec"]
val clientOutputDir = "$buildDir/Clients"

tasks.register(taskName, org.openapitools.generator.gradle.plugin.tasks.GenerateTask::class) {
    input = "$buildDir/$jsonFile"
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

sourceSets {
    val main by getting
    main.java.srcDir("$clientOutputDir/src/main/kotlin")
}

java {
    withSourcesJar()
}

tasks.bootJar {
    enabled = false
}

tasks.getByName("sourcesJar") {
    dependsOn(taskName)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    dependsOn(taskName)
}

ktlint {
    filter {
        exclude("**/edcClient/**")
    }
}
