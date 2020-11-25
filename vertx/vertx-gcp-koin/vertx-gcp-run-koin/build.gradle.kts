import gradle.dependencies.loadLocalProjects
import gradle.dependencies.publishingToS3

plugins {
    `java-library`

    id("cloud.maven-publishing")
}

loadLocalProjects(
    configuration = "api",
    projectNames = listOf(
        ":vertx:vertx-koin",
        ":vertx:vertx-web-koin",
        ":vertx:vertx-gcp-koin:vertx-gcp-core-koin"
    )
)

// PUBLISH TO MAVEN REPO
publishing {
    publishingToS3(this@publishing)
}
