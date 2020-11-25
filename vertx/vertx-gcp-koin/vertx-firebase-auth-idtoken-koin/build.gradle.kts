import gradle.dependencies.loadLocalProjects
import gradle.dependencies.publishingToS3
import gradle.dependencies.vertxAuthDependencies
import gradle.dependencies.vertxJwtDependencies

plugins {
    `java-library`

    id("cloud.maven-publishing")
}

vertxAuthDependencies("api")
vertxJwtDependencies("api")

loadLocalProjects(
    configuration = "api",
    projectNames = listOf(
        ":vertx:vertx-koin",
        ":vertx:vertx-web-koin",
        ":vertx:vertx-webclient-koin"
    )
)

// TESTING
loadLocalProjects(configuration = "testImplementation", projectNames = listOf(":vertx:vertx-test-koin",":vertx:vertx-boot:vertx-boot-http-server"))

// PUBLISH TO MAVEN REPO
publishing {
    publishingToS3(this@publishing)
}
