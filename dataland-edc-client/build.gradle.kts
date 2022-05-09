val sonarSources by extra(emptyList<File>())
val jacocoSources by extra(emptyList<File>())
val jacocoClasses by extra(emptyList<File>())

plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    kotlin("kapt")
    id("org.openapi.generator")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

val openApiSpecConfig by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}

java.sourceCompatibility = JavaVersion.VERSION_11

dependencies {
    implementation(libs.springdoc.openapi.ui)
    implementation("org.springframework:spring-context")
    implementation("org.springframework.boot:spring-boot")
    implementation("com.fasterxml.jackson.core:jackson-annotations")
    implementation(libs.moshi.kotlin)
    implementation(libs.moshi.adapters)
    implementation(libs.okhttp)
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
