import gradle.dependencies.CoreVersions.Vertx
import gradle.dependencies.import
import gradle.util.loadLocalProjects
import gradle.util.publishingToS3

plugins {
    id("common.kotlin-library")
}

loadLocalProjects(
    configuration = "api",
    projectNames = listOf(":vertx:vertx-koin:vertx-koin-core", ":util:fn-result", ":util:kotlin-logger")
)

dependencies {
    Vertx.webClientDependencies.import("api", this)
}

loadLocalProjects(configuration = "testImplementation", projectNames = listOf(":vertx:vertx-test-koin"))

publishing {
    publishingToS3(this@publishing)
}
