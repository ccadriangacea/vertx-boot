import gradle.dependencies.jsonDependencies
import gradle.dependencies.loadLocalProjects
import gradle.dependencies.publishingToS3
import gradle.dependencies.vertxConfigDependencies

plugins {
    id("app.kotlin-library-conventions")
}

loadLocalProjects(
    configuration = "api",
    projectNames = listOf(":koin:koin-core", ":util:fn-result", ":vertx:vertx-common")
)

vertxConfigDependencies("api")
jsonDependencies("api")

publishing {
    publishingToS3(this@publishing)
}
