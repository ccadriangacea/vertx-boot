import gradle.dependencies.CoreVersions.Vertx
import gradle.dependencies.import
import gradle.util.loadLocalProjects
import gradle.util.publishingToS3

plugins {
    id("common.kotlin-library")
}

loadLocalProjects(configuration = "api", projectNames = listOf(":vertx:vertx-koin:vertx-koin-core"))

dependencies {
    Vertx.mongodbDependencies.import("api", this)
}

// TESTING
loadLocalProjects(configuration = "testImplementation", projectNames = listOf(":vertx:vertx-test-koin", ":vertx:vertx-koin:vertx-webclient-koin"))

// PUBLISHING
publishing {
    publishingToS3(this@publishing)
}
