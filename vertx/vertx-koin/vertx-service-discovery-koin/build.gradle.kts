import gradle.dependencies.CoreVersions.Vertx
import gradle.dependencies.import
import gradle.util.loadLocalProjects
import gradle.util.publishingToS3

plugins {
    id("common.kotlin-library")
}

loadLocalProjects(configuration = "api", projectNames = listOf(":vertx:vertx-koin:vertx-koin-core"))

dependencies {
    Vertx.serviceDiscoveryDependencies.import("api", this)
}

publishing {
    publishingToS3(this@publishing)
}

