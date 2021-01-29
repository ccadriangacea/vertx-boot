import gradle.dependencies.CoreVersions.Vertx
import gradle.dependencies.GcpVersions.Firebase
import gradle.dependencies.import
import gradle.util.loadLocalProjects
import gradle.util.publishingToS3

plugins {
    id("common.kotlin-library")
}

loadLocalProjects(
    configuration = "api",
    projectNames = listOf(
        ":vertx:vertx-koin:vertx-koin-core",
        ":vertx:vertx-koin:vertx-webclient-koin"
    )
)

dependencies {
    Vertx.jwtDependencies.import("api", this)
    Vertx.webDependencies.import("api", this)

    Firebase.Admin.coreDependencies.import("api", this)
}

// TESTING

publishing {
    publishingToS3(this@publishing)
}
