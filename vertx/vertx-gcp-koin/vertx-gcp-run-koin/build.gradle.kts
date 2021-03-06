import gradle.util.loadLocalProjects
import gradle.util.publishingToS3

plugins {
    id("common.kotlin-library")
}

loadLocalProjects(
    configuration = "api",
    projectNames = listOf(
        ":vertx:vertx-koin:vertx-koin-core",
        ":vertx:vertx-koin:vertx-web-koin",
        ":vertx:vertx-gcp-koin:vertx-gcp-core-koin"
    )
)

// PUBLISH TO MAVEN REPO
publishing {
    publishingToS3(this@publishing)
}
