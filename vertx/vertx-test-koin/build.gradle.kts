import gradle.dependencies.Versions
import gradle.util.loadLocalProjects
import gradle.util.publishingToS3

plugins {
    id("common.kotlin-library")

    id("testing.junit-conventions")
}

loadLocalProjects(configuration = "api", projectNames = listOf(":vertx:vertx-boot:vertx-boot-instance"))

dependencies {
    "api"(Versions.Koin.testDependencies)
    "api"(Versions.Vertx.coreTestDependencies)
}

publishing {
    publishingToS3(this@publishing)
}

