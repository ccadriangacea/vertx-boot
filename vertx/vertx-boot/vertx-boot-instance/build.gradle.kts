import gradle.dependencies.loadLocalProjects
import gradle.dependencies.publishingToS3

plugins {
    `java-library`

    id("cloud.maven-publishing")
}

loadLocalProjects(configuration = "api", projectNames = listOf(":vertx:vertx-koin",":vertx:vertx-logger"))

loadLocalProjects(configuration = "testImplementation", projectNames = listOf(":vertx:vertx-test-koin"))

publishing {
    publishingToS3(this@publishing)
}
