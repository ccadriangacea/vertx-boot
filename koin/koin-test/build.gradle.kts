import gradle.dependencies.Versions
import gradle.util.loadLocalProjects
import gradle.util.publishingToS3

plugins {
    id("common.kotlin-library")
}

loadLocalProjects("api", listOf(":koin:koin-core"))

dependencies {
    "api"(Versions.Kotlin.reflect)
    "api"(Versions.Koin.testDependencies)
}

publishing {
    publishingToS3(this@publishing)
}
