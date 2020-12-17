import gradle.dependencies.junitDependencies
import gradle.dependencies.koinVertxTestDependencies
import gradle.dependencies.kotlinTestingDependencies
import gradle.dependencies.loadLocalProjects
import gradle.util.publishingToS3

plugins {
    id("common.kotlin-library")
}

loadLocalProjects(configuration = "api", projectNames = listOf(":vertx:vertx-boot:vertx-boot-instance"))

koinVertxTestDependencies("api")
kotlinTestingDependencies("api")
junitDependencies("api")

publishing {
    publishingToS3(this@publishing)
}

