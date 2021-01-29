import gradle.dependencies.CoreVersions.Vertx
import gradle.dependencies.GcpVersions
import gradle.dependencies.GcpVersions.PubSub
import gradle.dependencies.import
import gradle.util.loadLocalProjects
import gradle.util.publishingToS3

plugins {
    id("common.kotlin-library")
}

loadLocalProjects(configuration = "api", projectNames = listOf(":vertx:vertx-koin:vertx-koin-core", ":vertx:vertx-gcp-koin:vertx-gcp-core-koin"))

dependencies {
    "api"(platform(GcpVersions.bomDependencies))

    PubSub.coreDependencies.import("api", this)
    Vertx.webDependencies.import("api", this)
}

// PUBLISH TO MAVEN REPO
publishing {
    publishingToS3(this@publishing)
}
