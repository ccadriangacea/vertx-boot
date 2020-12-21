import gradle.dependencies.Versions
import gradle.util.loadLocalProjects
import gradle.util.publishingToS3

plugins {
    id("common.kotlin-library")
}

loadLocalProjects(configuration = "api", projectNames = listOf(":vertx:vertx-koin:vertx-koin-core"))

dependencies {
    Versions.Vertx.jwtDepdendencies.forEach { "api"(it) }
    Versions.Vertx.oauth2Dependencies.forEach { "api"(it) }

    "api"(Versions.Vertx.webDependencies)
}

loadLocalProjects(configuration = "testImplementation", projectNames = listOf(":vertx:vertx-test-koin"))

publishing {
    publishingToS3(this@publishing)
}
