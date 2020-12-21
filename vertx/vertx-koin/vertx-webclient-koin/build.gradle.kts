import gradle.util.loadLocalProjects
import gradle.util.publishingToS3
import gradle.dependencies.Versions

plugins {
    id("common.kotlin-library")
}

loadLocalProjects(
    configuration = "api",
    projectNames = listOf(":vertx:vertx-koin:vertx-koin-core", ":util:fn-result", ":util:kotlin-logger")
)

dependencies {
    "api"(Versions.Vertx.webClientDependencies)
}

loadLocalProjects(configuration = "testImplementation", projectNames = listOf(":vertx:vertx-test-koin"))

publishing {
    publishingToS3(this@publishing)
}
