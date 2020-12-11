import gradle.dependencies.loadLocalProjects
import gradle.dependencies.publishingToS3
import gradle.dependencies.vertxServiceDiscoveryDependencies

plugins {
    id("app.kotlin-library-conventions")
}

vertxServiceDiscoveryDependencies("api")

loadLocalProjects(configuration = "api", projectNames = listOf(":vertx:vertx-koin:vertx-koin-core"))

publishing {
    publishingToS3(this@publishing)
}

