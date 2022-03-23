allprojects {
    repositories {
        mavenCentral()
    }
    group = "org.datalandedc"
    val releaseTagPrefix = "RELEASE-"
    val refName = System.getenv("GITHUB_REF") ?: ""
    val isRelease = (System.getenv("GITHUB_REF_TYPE") ?: "") == "tag" && refName.substringAfterLast("/")
        .startsWith(releaseTagPrefix)
    version = if (isRelease) {
        val releaseVersion = refName.substringAfterLast("/").substring(releaseTagPrefix.length)
        println("Running gradle in RELEASE mode for Version $releaseVersion")
        releaseVersion
    } else {
        val devVersion = "0.0.1-SNAPSHOT"
        println("Running gradle in non-release mode for Version $devVersion")
        devVersion
    }
}

extra["OpenApiSpec"] = "OpenApiSpec.json"

subprojects {
    //apply(plugin = "org.springframework.boot")
    //apply(plugin = "io.spring.dependency-management")
    //apply(plugin = "org.openapi.generator")
    //apply(plugin = "java-library")
    apply(plugin = "maven-publish")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    //group = "org.dataland"
    //version = "0.0.1-SNAPSHOT"
}

plugins {
    id("io.gitlab.arturbosch.detekt") version "1.19.0"
    id("org.jlleitschuh.gradle.ktlint") version "10.2.1"
    kotlin("jvm") version "1.6.10"
}

dependencies {
    detekt("io.gitlab.arturbosch.detekt:detekt-cli:1.19.0")
    detekt("org.jetbrains.kotlin:kotlin-compiler-embeddable:1.6.10")
}

detekt {
    buildUponDefaultConfig = true
    allRules = false
    config = files("$projectDir/config/detekt.yml")
    baseline = file("$projectDir/config/baseline.xml")
    val detektFileTree = fileTree("$projectDir")
    detektFileTree.exclude("**/build/**").exclude("**/node_modules/**")
        .exclude(".gradle").exclude("**/DataSpaceConnector/**")
    source = files(detektFileTree)
}

ktlint {
    filter {
        exclude("**/openApiClient/**")
        exclude("**/openApiServer/**")
    }
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
