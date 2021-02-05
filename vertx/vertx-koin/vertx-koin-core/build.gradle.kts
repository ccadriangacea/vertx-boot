import gradle.dependencies.CoreVersions.Vertx
import gradle.util.loadLocalProjects
import gradle.util.publishingToS3

plugins {
    id("common.kotlin-library")
}

loadLocalProjects(configuration = "api", projectNames = listOf(":koin:koin-core", ":util:fn-result", ":vertx:vertx-common"))

loadLocalProjects(configuration = "testImplementation", projectNames = listOf(":koin:koin-test"))

dependencies {
    Vertx.configDependencies.forEach { "api"(it) }
}

publishing {
    publishingToS3(this@publishing)
}
