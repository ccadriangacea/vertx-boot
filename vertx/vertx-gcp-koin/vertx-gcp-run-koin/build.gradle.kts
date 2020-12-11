import gradle.dependencies.loadLocalProjects
import gradle.dependencies.publishingToS3

plugins {
    id("app.kotlin-library-conventions")
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
