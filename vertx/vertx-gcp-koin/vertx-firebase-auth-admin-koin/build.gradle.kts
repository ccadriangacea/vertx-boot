import gradle.dependencies.gcpFirebaseAdminDependencies
import gradle.dependencies.loadLocalProjects
import gradle.util.publishingToS3
import gradle.dependencies.vertxAuthDependencies
import gradle.dependencies.vertxJwtDependencies
import gradle.dependencies.vertxWebDependencies

plugins {
    id("common.kotlin-library")
}

gcpFirebaseAdminDependencies()

vertxWebDependencies("api")
vertxAuthDependencies("api")
vertxJwtDependencies("api")

loadLocalProjects(
    configuration = "api",
    projectNames = listOf(
        ":vertx:vertx-koin:vertx-koin-core",
        ":vertx:vertx-koin:vertx-webclient-koin"
    )
)

// TESTING


// PUBLISH TO MAVEN REPO
publishing {
    publishingToS3(this@publishing)
}
