val sonarSources by extra(emptyList<File>())
val jacocoSources by extra(emptyList<File>())
val jacocoClasses by extra(emptyList<File>())
plugins {
    id("org.openapi.generator") version "5.4.0"
}

val destinationPackage = "org.dataland.datalandbackend.euroDatClient"
val output = "$buildDir/Clients"
val spec = rootProject.extra["OpenApiSpecEuroDat"]
val taskName = "generateEuroDatClient"

tasks.register(taskName, org.openapitools.generator.gradle.plugin.tasks.GenerateTask::class) {
    input = "$rootDir/resources/$spec"
    outputDir.set(output)
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
}

sourceSets {
    val main by getting
    main.java.srcDir(output)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    dependsOn(taskName)
}
