import gradle.dependencies.jsonDependencies
import gradle.dependencies.koinDependencies
import gradle.dependencies.kotlinLoggingDependencies
import gradle.dependencies.loadLocalProjects
import gradle.dependencies.publishingToS3
import gradle.dependencies.vertxConfigDependencies

plugins {
    `java-library`

    id("cloud.maven-publishing")
}

koinDependencies("api")

loadLocalProjects(configuration = "api", projectNames = listOf(":vertx:vertx-common"))

vertxConfigDependencies("api")
jsonDependencies("api")

publishing {
    publishingToS3(this@publishing)
}
