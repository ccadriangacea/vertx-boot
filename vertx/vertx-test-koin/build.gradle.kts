import gradle.dependencies.Versions
import gradle.util.loadLocalProjects
import gradle.util.publishingToS3

plugins {
    id("common.kotlin-library")
}

loadLocalProjects(configuration = "api", projectNames = listOf(":vertx:vertx-boot:vertx-boot-instance"))

dependencies {
    "api"(Versions.Koin.testDependencies)
}

publishing {
    publishingToS3(this@publishing)
}

