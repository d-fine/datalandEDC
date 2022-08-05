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
val jacocoVersion: String by project

repositories {
    mavenCentral()
}

plugins {
    id("org.springframework.boot")
    kotlin("jvm")
    jacoco
    kotlin("plugin.spring")
    id("org.springdoc.openapi-gradle-plugin")
    id("org.openapi.generator")
}

apply(plugin = "io.spring.dependency-management")

jacoco {
    toolVersion = jacocoVersion
}

tasks.test {
    useJUnitPlatform()

    extensions.configure(JacocoTaskExtension::class) {
        setDestinationFile(file("$buildDir/jacoco/jacoco.exec"))
    }

    dependsOn("generateEdcServer")
}

java.sourceCompatibility = JavaVersion.VERSION_11

val openApiSpecConfig by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}

dependencies {
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation(libs.springdoc.openapi.ui)
    implementation(libs.junit.jupiter)
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    compileOnly(libs.jakarta.annotation.api)

    openApiSpecConfig(project(mapOf("path" to ":dataland-edc-server", "configuration" to "openApiSpec")))
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
