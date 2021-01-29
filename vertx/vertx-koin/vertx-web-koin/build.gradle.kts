import gradle.dependencies.CoreVersions.Vertx
import gradle.dependencies.import
import gradle.util.loadLocalProjects
import gradle.util.publishingToS3

plugins {
    id("common.kotlin-library")
}

loadLocalProjects(configuration = "api", projectNames = listOf(":vertx:vertx-koin:vertx-koin-core"))

dependencies {
    Vertx.jwtDependencies.import("api", this)
    Vertx.oauth2Dependencies.import("api", this)
    Vertx.webDependencies.import("api", this)
}

loadLocalProjects(configuration = "testImplementation", projectNames = listOf(":vertx:vertx-test-koin"))

publishing {
    publishingToS3(this@publishing)
}
