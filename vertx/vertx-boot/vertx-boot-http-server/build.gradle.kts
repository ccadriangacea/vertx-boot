import gradle.dependencies.loadLocalProjects
import gradle.dependencies.publishingToS3

plugins {
    `java-library`

    id("cloud.maven-publishing")
}

loadLocalProjects(
    configuration = "api",
    projectNames = listOf(
        ":vertx:vertx-boot:vertx-boot-instance",
        ":vertx:vertx-web-koin"
    )
)

// TESTING
loadLocalProjects(configuration = "testImplementation", projectNames = listOf(":vertx:vertx-test-koin", ":vertx:vertx-webclient-koin"))

// PUBLISHING
publishing {
    publishingToS3(this@publishing)
}
