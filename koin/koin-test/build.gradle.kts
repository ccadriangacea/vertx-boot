import gradle.dependencies.junitDependencies
import gradle.dependencies.koinTestDependencies
import gradle.dependencies.kotlinReflectDependencies
import gradle.dependencies.loadLocalProjects
import gradle.util.publishingToS3

plugins {
    id("common.kotlin-library")
}

loadLocalProjects("api", listOf(":koin:koin-core"))

kotlinReflectDependencies("api")
koinTestDependencies("api")
junitDependencies("api")

publishing {
    publishingToS3(this@publishing)
}
