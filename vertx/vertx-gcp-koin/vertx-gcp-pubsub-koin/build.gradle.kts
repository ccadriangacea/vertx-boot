import gradle.dependencies.GcpVersions
import gradle.dependencies.Versions
import gradle.util.loadLocalProjects
import gradle.util.publishingToS3

plugins {
    id("common.kotlin-library")
}

loadLocalProjects(configuration = "api", projectNames = listOf(":vertx:vertx-koin:vertx-koin-core", ":vertx:vertx-gcp-koin:vertx-gcp-core-koin"))

dependencies {
    "api"(platform(GcpVersions.bomDependencies))
    "api"(platform(GcpVersions.PubSub.dependencies))
    "api"(Versions.Vertx.webDependencies)
}

// PUBLISH TO MAVEN REPO
publishing {
    publishingToS3(this@publishing)
}
