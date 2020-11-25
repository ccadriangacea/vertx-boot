import gradle.dependencies.gcpAuthDependencies
import gradle.dependencies.gcpGaxDependencies
import gradle.dependencies.loadLocalProjects
import gradle.dependencies.publishingToS3

plugins {
    `java-library`

    id("cloud.maven-publishing")
}

gcpAuthDependencies("api")
gcpGaxDependencies("api")

loadLocalProjects(configuration = "api", projectNames = listOf(":vertx:vertx-koin"))

// PUBLISH TO MAVEN REPO
publishing {
    publishingToS3(this@publishing)
}
