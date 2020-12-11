import gradle.dependencies.loadLocalProjects
import gradle.dependencies.publishingToS3
import gradle.dependencies.vertxWebClientDependencies

plugins {
    id("app.kotlin-library-conventions")
}

vertxWebClientDependencies("api")

loadLocalProjects(
    configuration = "api",
    projectNames = listOf(":vertx:vertx-koin:vertx-koin-core", ":util:fn-result", ":util:kotlin-logger")
)

loadLocalProjects(configuration = "testImplementation", projectNames = listOf(":vertx:vertx-test-koin"))

publishing {
    publishingToS3(this@publishing)
}
