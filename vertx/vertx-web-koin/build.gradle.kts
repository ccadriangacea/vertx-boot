import gradle.dependencies.loadLocalProjects
import gradle.dependencies.publishingToS3
import gradle.dependencies.vertxJwtDependencies
import gradle.dependencies.vertxOauth2Dependencies
import gradle.dependencies.vertxWebDependencies

plugins {
    `java-library`

    id("cloud.maven-publishing")
}

vertxWebDependencies("api")
vertxJwtDependencies("api")
vertxOauth2Dependencies("api")

loadLocalProjects(configuration = "api", projectNames = listOf(":vertx:vertx-koin"))

loadLocalProjects(configuration = "testImplementation", projectNames = listOf(":vertx:vertx-test-koin"))

publishing {
    publishingToS3(this@publishing)
}
