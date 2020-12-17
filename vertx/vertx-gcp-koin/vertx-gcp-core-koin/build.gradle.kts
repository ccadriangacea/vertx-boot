import gradle.dependencies.gcpAuthDependencies
import gradle.dependencies.gcpGaxDependencies
import gradle.dependencies.loadLocalProjects
import gradle.util.publishingToS3

plugins {
    id("common.kotlin-library")
}

gcpAuthDependencies("api")
gcpGaxDependencies("api")

loadLocalProjects(configuration = "api", projectNames = listOf(":vertx:vertx-koin:vertx-koin-core"))

// PUBLISH TO MAVEN REPO
publishing {
    publishingToS3(this@publishing)
}
