import gradle.dependencies.GcpVersions
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
        ":vertx:vertx-koin:vertx-webclient-koin"
    )
)

dependencies {
    Versions.Vertx.jwtDepdendencies.forEach { "api"(it) }
    "api"(Versions.Vertx.webDependencies)

    "api"(GcpVersions.Firebase.Admin.depdendencies)
}

// TESTING

publishing {
    publishingToS3(this@publishing)
}
