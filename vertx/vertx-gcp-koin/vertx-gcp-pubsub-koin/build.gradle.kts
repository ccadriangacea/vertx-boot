import gradle.dependencies.gcpBomDependencies
import gradle.dependencies.gcpPubSubDependencies
import gradle.dependencies.loadLocalProjects
import gradle.dependencies.publishingToS3
import gradle.dependencies.vertxWebDependencies

plugins {
    id("app.kotlin-library-conventions")
}

gcpBomDependencies("api")
gcpPubSubDependencies("api")

vertxWebDependencies("implementation")

loadLocalProjects(configuration = "api", projectNames = listOf(":vertx:vertx-koin:vertx-koin-core", ":vertx:vertx-gcp-koin:vertx-gcp-core-koin"))

// PUBLISH TO MAVEN REPO
publishing {
    publishingToS3(this@publishing)
}
