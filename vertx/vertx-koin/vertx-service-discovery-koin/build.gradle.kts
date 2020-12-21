import gradle.dependencies.Versions
import gradle.util.loadLocalProjects
import gradle.util.publishingToS3

plugins {
    id("common.kotlin-library")
}

loadLocalProjects(configuration = "api", projectNames = listOf(":vertx:vertx-koin:vertx-koin-core"))

dependencies {
    "api"(Versions.Vertx.serviceDiscoveryDependencies)
}

publishing {
    publishingToS3(this@publishing)
}

