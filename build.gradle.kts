// main

allprojects {
    repositories {
        mavenCentral()
        maven("https://maven.iais.fraunhofer.de/artifactory/eis-ids-public")
    }
    group = "org.dataland"
    val releaseTagPrefix = "RELEASE-"
    val refName = System.getenv("GITHUB_REF") ?: ""
    val isRelease = (System.getenv("GITHUB_REF_TYPE") ?: "") == "tag" && refName.substringAfterLast("/")
        .startsWith(releaseTagPrefix)
    version = if (isRelease) {
        val releaseVersion = refName.substringAfterLast("/").substring(releaseTagPrefix.length)
        println("Running gradle in RELEASE mode for Version $releaseVersion")
        releaseVersion
    } else {
        val devVersion = "0.0.2-SNAPSHOT"
        println("Running gradle in non-release mode for Version $devVersion")
        devVersion
    }
}

val connectorVersion = "0.0.1"

extra["OpenApiSpec"] = "OpenApiSpec.json"

subprojects {
    sonarqube {
        isSkipProject = true
    }
    apply(plugin = "maven-publish")
    apply(plugin = "java-library")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict", "-opt-in=kotlin.RequiresOptIn")
            jvmTarget = "11"
        }
    }
    configure<PublishingExtension> {
        repositories {
            maven {
                name = "GitHubPackages"
                url = uri("https://maven.pkg.github.com/d-fine/datalandEDC")
                credentials {
                    username = System.getenv("GITHUB_ACTOR")
                    password = System.getenv("GITHUB_TOKEN")
                }
            }
        }
        publications {
            register<MavenPublication>("gpr") {
                from(components["java"])
            }
        }
    }
}

plugins {
    id("org.springframework.boot") version "2.6.7" apply false
    id("io.gitlab.arturbosch.detekt") version "1.20.0"
    id("org.jlleitschuh.gradle.ktlint") version "10.2.1"
    kotlin("jvm") version "1.6.21"
    kotlin("plugin.spring") version "1.6.21" apply false
    id("org.sonarqube") version "3.3"
    id("org.openapi.generator") version "5.4.0" apply false
    id("org.springdoc.openapi-gradle-plugin") version "1.3.4" apply false
    id("io.swagger.core.v3.swagger-gradle-plugin") version "2.2.0" apply false
    jacoco
}

ktlint {
    filter {
        exclude("**/trustee-platform/**, **/dataland-eurodat-dummyserver/**, **/dataland-eurodat-client/**")
    }
}

sonarqube {
    properties {
        property("sonar.projectKey", "d-fine_datalandEDC")
        property("sonar.organization", "d-fine")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.qualitygate.wait", true)
        property(
            "sonar.coverage.exclusions",
            "**/test/**, **/trustee-platform/**, **/dataland-eurodat-dummyserver/**, " +
                "**/dataland-eurodat-client/**, **/extensions/**, **/DummyEdc.kt"
        )
        property(
            "sonar.sources",
            subprojects.flatMap { project -> project.properties["sonarSources"] as Iterable<*> }
        )
    }
}

jacoco {
    toolVersion = "0.8.7"
}

tasks.jacocoTestReport {
    dependsOn(tasks.build)
    sourceDirectories.setFrom(
        subprojects.flatMap { project -> project.properties["jacocoSources"] as Iterable<*> }
    )
    classDirectories.setFrom(
        subprojects.flatMap { project -> project.properties["jacocoClasses"] as Iterable<*> }
    )
    reports {
        xml.required.set(true)
        csv.required.set(false)
    }
    executionData.setFrom(fileTree(projectDir).include("**/*.exec"))
}

dependencies {
    detekt("io.gitlab.arturbosch.detekt:detekt-cli:1.20.0")
    detekt("org.jetbrains.kotlin:kotlin-compiler-embeddable:1.6.21")
}

detekt {
    buildUponDefaultConfig = true
    allRules = false
    config = files("$projectDir/config/detekt.yml")
    val detektFileTree = fileTree("$projectDir")
    detektFileTree
        .exclude("**/build/**")
        .exclude("**/trustee-platform/**")
        .exclude("**/dataland-eurodat-dummyserver/**")
        .exclude("**/dataland-eurodat-client/**")
        .exclude("**/node_modules/**")
        .exclude(".gradle")
    source = files(detektFileTree)
}

tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    reports {
        html.required.set(true)
        xml.required.set(true)
        txt.required.set(true)
        sarif.required.set(true)
    }
    jvmTarget = java.sourceCompatibility.toString()
}
tasks.withType<io.gitlab.arturbosch.detekt.DetektCreateBaselineTask>().configureEach {
    jvmTarget = java.sourceCompatibility.toString()
}
