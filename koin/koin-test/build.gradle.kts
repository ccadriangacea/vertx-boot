import gradle.dependencies.CoreVersions.Koin
import gradle.dependencies.CoreVersions.Kotlin
import gradle.dependencies.import
import gradle.util.loadLocalProjects
import gradle.util.publishingToS3

plugins {
    id("common.kotlin-library")
}

loadLocalProjects("api", listOf(":koin:koin-core"))

dependencies {
    Kotlin.reflect.import("api", this)
    Koin.testDependencies.import("api", this)
}

publishing {
    publishingToS3(this@publishing)
}
