import gradle.dependencies.loadLocalProjects
import gradle.dependencies.publishingToS3

plugins {
    id("app.kotlin-library-conventions")
}

loadLocalProjects(configuration = "api", projectNames = listOf(":vertx:vertx-koin:vertx-koin-core", ":util:kotlin-logger"))

loadLocalProjects(configuration = "testImplementation", projectNames = listOf(":vertx:vertx-test-koin"))

publishing {
    publishingToS3(this@publishing)
}
