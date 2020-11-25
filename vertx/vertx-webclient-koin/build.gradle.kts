import gradle.dependencies.loadLocalProjects
import gradle.dependencies.publishingToS3
import gradle.dependencies.vertxWebClientDependencies

plugins {
    `java-library`

    id("cloud.maven-publishing")
}

vertxWebClientDependencies("api")

loadLocalProjects(configuration = "api", projectNames = listOf(":vertx:vertx-koin"))

loadLocalProjects(configuration = "testImplementation", projectNames = listOf(":vertx:vertx-test-koin"))

publishing {
    publishingToS3(this@publishing)
}
