// main
val jacocoVersion: String by project
val connectorVersion: String by project
val ktlintVersion: String by project

allprojects {
    repositories {
        mavenLocal()
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

extra["OpenApiSpec"] = "OpenApiSpec.json"

subprojects {
    sonarqube {
        isSkipProject = true
    }
    apply(plugin = "maven-publish")
    apply(plugin = "java-library")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    ktlint {
        version.set(ktlintVersion)
    }
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
    id("org.springframework.boot") version "2.7.5" apply false
    id("io.gitlab.arturbosch.detekt") version "1.22.0-RC3"
    id("org.jlleitschuh.gradle.ktlint") version "11.0.0"
    kotlin("jvm") version "1.7.21"
    kotlin("plugin.spring") version "1.7.21" apply false
    id("org.sonarqube") version "3.5.0.2730"
    id("org.openapi.generator") version "6.2.1" apply false
    id("org.springdoc.openapi-gradle-plugin") version "1.4.0" apply false
    id("io.swagger.core.v3.swagger-gradle-plugin") version "2.2.6" apply false
    jacoco
    id("com.github.ben-manes.versions") version "0.43.0"
    id("com.github.johnrengelman.shadow") version "7.1.2" apply false
}

tasks.dependencyUpdates.configure {
    gradleReleaseChannel = "current"
}

sonarqube {
    properties {
        property("sonar.projectKey", "d-fine_datalandEDC")
        property("sonar.organization", "d-fine")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.qualitygate.wait", true)
        property(
            "sonar.coverage.exclusions",
            "**/test/**, **/DummyEdc.kt"
        )
        property(
            "sonar.sources",
            subprojects.flatMap { project -> project.properties["sonarSources"] as Iterable<*> }
        )
    }
}

ktlint {
    version.set(ktlintVersion)
}

jacoco {
    toolVersion = jacocoVersion
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
    detekt("io.gitlab.arturbosch.detekt:detekt-cli:1.22.0-RC3")
    detekt("org.jetbrains.kotlin:kotlin-compiler-embeddable:1.7.20")
}

detekt {
    buildUponDefaultConfig = true
    allRules = false
    config = files("$projectDir/config/detekt.yml")
    val detektFileTree = fileTree("$projectDir")
    detektFileTree
        .exclude("**/build/**")
        .exclude("**/EDC/**")
        .exclude("**/EDCGradlePlugins/**")
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
