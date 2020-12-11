import gradle.dependencies.loadLocalProjects
import gradle.dependencies.publishingToS3
import gradle.dependencies.vertxJwtDependencies
import gradle.dependencies.vertxOauth2Dependencies
import gradle.dependencies.vertxWebDependencies

plugins {
    id("app.kotlin-library-conventions")
}

vertxWebDependencies("api")
vertxJwtDependencies("api")
vertxOauth2Dependencies("api")

loadLocalProjects(configuration = "api", projectNames = listOf(":vertx:vertx-koin:vertx-koin-core"))

loadLocalProjects(configuration = "testImplementation", projectNames = listOf(":vertx:vertx-test-koin"))

publishing {
    publishingToS3(this@publishing)
}
