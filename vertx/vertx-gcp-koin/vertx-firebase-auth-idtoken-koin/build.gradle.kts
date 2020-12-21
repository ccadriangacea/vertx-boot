import gradle.dependencies.Versions
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
        ":vertx:vertx-koin:vertx-webclient-koin"
    )
)

dependencies {
    Versions.Vertx.jwtDepdendencies.forEach { "api"(it) }
}

// TESTING
loadLocalProjects(configuration = "testImplementation", projectNames = listOf(":vertx:vertx-test-koin", ":vertx:vertx-boot:vertx-boot-http-server"))

// PUBLISH TO MAVEN REPO
publishing {
    publishingToS3(this@publishing)
}
