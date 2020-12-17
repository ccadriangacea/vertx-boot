import gradle.dependencies.loadLocalProjects
import gradle.util.publishingToS3
import gradle.dependencies.vertxServiceDiscoveryDependencies

plugins {
    id("common.kotlin-library")
}

vertxServiceDiscoveryDependencies("api")

loadLocalProjects(configuration = "api", projectNames = listOf(":vertx:vertx-koin:vertx-koin-core"))

publishing {
    publishingToS3(this@publishing)
}

