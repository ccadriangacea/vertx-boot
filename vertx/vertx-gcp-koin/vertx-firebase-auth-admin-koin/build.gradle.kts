import gradle.dependencies.gcpFirebaseAdminDependencies
import gradle.dependencies.loadLocalProjects
import gradle.dependencies.publishingToS3
import gradle.dependencies.vertxAuthDependencies
import gradle.dependencies.vertxJwtDependencies
import gradle.dependencies.vertxWebDependencies

plugins {
    `java-library`

    id("cloud.maven-publishing")
}

gcpFirebaseAdminDependencies()

vertxWebDependencies("api")
vertxAuthDependencies("api")
vertxJwtDependencies("api")

loadLocalProjects(
    configuration = "api",
    projectNames = listOf(
        ":vertx:vertx-koin",
        ":vertx:vertx-webclient-koin"
    )
)

// TESTING


// PUBLISH TO MAVEN REPO
publishing {
    publishingToS3(this@publishing)
}
