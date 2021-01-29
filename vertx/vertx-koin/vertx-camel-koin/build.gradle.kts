import gradle.dependencies.LibrariesVersions.Camel
import gradle.dependencies.import
import gradle.util.loadLocalProjects
import gradle.util.publishingToS3

plugins {
    id("common.kotlin-library")
}

loadLocalProjects(configuration = "api", projectNames = listOf(":vertx:vertx-koin:vertx-koin-core"))

dependencies {
    Camel.coreDependencies.import("api", this)
    Camel.vertxDependencies.import("api", this)

    "testImplementation"("com.jayway.awaitility:awaitility:1.7.0")
}

// TESTING
loadLocalProjects(configuration = "testImplementation", projectNames = listOf(":vertx:vertx-test-koin", ":vertx:vertx-koin:vertx-webclient-koin"))

// PUBLISHING
publishing {
    publishingToS3(this@publishing)
}
