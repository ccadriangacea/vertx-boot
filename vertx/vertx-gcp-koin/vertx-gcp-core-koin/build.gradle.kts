import gradle.dependencies.gcpAuthDependencies
import gradle.dependencies.gcpGaxDependencies
import gradle.dependencies.loadLocalProjects
import gradle.dependencies.publishingToS3

plugins {
    id("app.kotlin-library-conventions")
}

gcpAuthDependencies("api")
gcpGaxDependencies("api")

loadLocalProjects(configuration = "api", projectNames = listOf(":vertx:vertx-koin:vertx-koin-core"))

// PUBLISH TO MAVEN REPO
publishing {
    publishingToS3(this@publishing)
}
