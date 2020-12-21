import gradle.dependencies.GcpVersions
import gradle.util.loadLocalProjects
import gradle.util.publishingToS3

plugins {
    id("common.kotlin-library")
}

loadLocalProjects(configuration = "api", projectNames = listOf(":vertx:vertx-koin:vertx-koin-core"))

dependencies {
    GcpVersions.Auth.dependencies.forEach { "api"(it) }
}

// PUBLISH TO MAVEN REPO
publishing {
    publishingToS3(this@publishing)
}
