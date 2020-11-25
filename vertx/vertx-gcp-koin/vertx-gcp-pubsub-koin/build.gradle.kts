import gradle.dependencies.gcpBomDependencies
import gradle.dependencies.gcpPubSubDependencies
import gradle.dependencies.loadLocalProjects
import gradle.dependencies.publishingToS3
import gradle.dependencies.vertxWebDependencies

plugins {
    `java-library`

    id("cloud.maven-publishing")
}

gcpBomDependencies("api")
gcpPubSubDependencies("api")

vertxWebDependencies("implementation")

loadLocalProjects(configuration = "api", projectNames = listOf(":vertx:vertx-koin", ":vertx:vertx-gcp-koin:vertx-gcp-core-koin"))

// PUBLISH TO MAVEN REPO
publishing {
    publishingToS3(this@publishing)
}
