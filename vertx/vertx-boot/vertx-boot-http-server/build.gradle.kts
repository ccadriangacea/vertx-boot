import gradle.dependencies.loadLocalProjects
import gradle.dependencies.publishingToS3

plugins {
    id( "app.kotlin-library-conventions")
}

loadLocalProjects(
    configuration = "api",
    projectNames = listOf(
        ":vertx:vertx-boot:vertx-boot-instance",
        ":vertx:vertx-koin:vertx-web-koin"
    )
)

// TESTING
loadLocalProjects(configuration = "testImplementation", projectNames = listOf(":vertx:vertx-test-koin", ":vertx:vertx-koin:vertx-webclient-koin"))

// PUBLISHING
publishing {
    publishingToS3(this@publishing)
}
