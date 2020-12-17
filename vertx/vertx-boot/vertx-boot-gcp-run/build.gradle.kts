import gradle.dependencies.loadLocalProjects
import gradle.util.publishingToS3

plugins {
    id("common.kotlin-library")
}

loadLocalProjects(
    configuration = "api",
    projectNames = listOf(
        ":vertx:vertx-boot:vertx-boot-http-server",
        ":vertx:vertx-gcp-koin:vertx-gcp-run-koin"
    )
)

// TESTING
loadLocalProjects(configuration = "testImplementation", projectNames = listOf(":vertx:vertx-test-koin"))

// PUBLISH TO MAVEN REPO
publishing {
    publishingToS3(this@publishing)
}
