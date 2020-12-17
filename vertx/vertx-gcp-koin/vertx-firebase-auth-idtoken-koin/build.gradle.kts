import gradle.dependencies.loadLocalProjects
import gradle.util.publishingToS3
import gradle.dependencies.vertxAuthDependencies
import gradle.dependencies.vertxJwtDependencies

plugins {
    id("common.kotlin-library")
}

vertxAuthDependencies("api")
vertxJwtDependencies("api")

loadLocalProjects(
    configuration = "api",
    projectNames = listOf(
        ":vertx:vertx-koin:vertx-koin-core",
        ":vertx:vertx-koin:vertx-web-koin",
        ":vertx:vertx-koin:vertx-webclient-koin"
    )
)

// TESTING
loadLocalProjects(configuration = "testImplementation", projectNames = listOf(":vertx:vertx-test-koin",":vertx:vertx-boot:vertx-boot-http-server"))

// PUBLISH TO MAVEN REPO
publishing {
    publishingToS3(this@publishing)
}
