import gradle.dependencies.Versions
import gradle.util.loadLocalProjects
import gradle.util.publishingToS3

plugins {
    id("common.kotlin-library")
}

loadLocalProjects(
    configuration = "api",
    projectNames = listOf(":koin:koin-core", ":util:fn-result", ":vertx:vertx-common")
)

dependencies {
    Versions.Vertx.configDependencies.forEach { "api"(it) }
}

publishing {
    publishingToS3(this@publishing)
}
