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
    id("org.springframework.boot")
    kotlin("jvm")
    jacoco
    kotlin("plugin.spring")
    id("org.springdoc.openapi-gradle-plugin")
    id("org.openapi.generator")
}

apply(plugin = "io.spring.dependency-management")

jacoco {
    toolVersion = "0.8.7"
}

tasks.test {
    useJUnitPlatform()

    extensions.configure(JacocoTaskExtension::class) {
        setDestinationFile(file("$buildDir/jacoco/jacoco.exec"))
    }

    dependsOn("generateEdcServer")
}

java.sourceCompatibility = JavaVersion.VERSION_17

val openApiSpecConfig by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation(libs.srpingdoc.openapi.ui)
    implementation(libs.junit.jupiter)
    testImplementation("org.springframework.boot:spring-boot-starter-test")
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
