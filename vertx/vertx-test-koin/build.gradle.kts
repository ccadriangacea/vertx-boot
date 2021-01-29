import gradle.dependencies.CoreVersions.Koin
import gradle.dependencies.CoreVersions.Vertx
import gradle.dependencies.import
import gradle.util.loadLocalProjects
import gradle.util.publishingToS3

plugins {
    id("common.kotlin-library")

    id("testing.junit-conventions")
}

loadLocalProjects(configuration = "api", projectNames = listOf(":vertx:vertx-boot:vertx-boot-instance"))

dependencies {
    Koin.testDependencies.import("api", this)
    Vertx.coreTestDependencies.import("api", this)
}

publishing {
    publishingToS3(this@publishing)
}

