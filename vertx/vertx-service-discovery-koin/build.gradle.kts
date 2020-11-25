import gradle.dependencies.kotlinTestingDependencies
import gradle.dependencies.loadLocalProjects
import gradle.dependencies.publishingToS3
import gradle.dependencies.vertxServiceDiscoveryDependencies

plugins {
    `java-library`

    id("cloud.maven-publishing")
}

vertxServiceDiscoveryDependencies("api")

loadLocalProjects(configuration = "api", projectNames = listOf(":vertx:vertx-koin"))

kotlinTestingDependencies("testImplementation")

publishing {
    publishingToS3(this@publishing)
}

