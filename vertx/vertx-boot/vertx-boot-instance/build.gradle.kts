import gradle.util.loadLocalProjects
import gradle.util.publishingToS3

plugins {
    id("common.kotlin-library")
}

loadLocalProjects(configuration = "api", projectNames = listOf(":vertx:vertx-koin:vertx-koin-core", ":util:kotlin-logger"))

loadLocalProjects(configuration = "testImplementation", projectNames = listOf(":vertx:vertx-test-koin"))

publishing {
    publishingToS3(this@publishing)
}
