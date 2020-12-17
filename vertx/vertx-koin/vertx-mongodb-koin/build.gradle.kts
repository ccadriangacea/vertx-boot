import gradle.dependencies.loadLocalProjects
import gradle.util.publishingToS3
import gradle.dependencies.vertxMongodbDependencies

plugins {
    id("common.kotlin-library")
}

vertxMongodbDependencies("api")

loadLocalProjects(configuration = "api", projectNames = listOf(":vertx:vertx-koin:vertx-koin-core"))

// TESTING
loadLocalProjects(configuration = "testImplementation", projectNames = listOf(":vertx:vertx-test-koin", ":vertx:vertx-koin:vertx-webclient-koin"))

// PUBLISHING
publishing {
    publishingToS3(this@publishing)
}
