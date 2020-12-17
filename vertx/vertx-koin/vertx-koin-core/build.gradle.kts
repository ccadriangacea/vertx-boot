import gradle.dependencies.jsonDependencies
import gradle.dependencies.loadLocalProjects
import gradle.util.publishingToS3
import gradle.dependencies.vertxConfigDependencies

plugins {
    id("common.kotlin-library")
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
