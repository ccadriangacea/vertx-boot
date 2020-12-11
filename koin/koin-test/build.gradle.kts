import gradle.dependencies.junitDependencies
import gradle.dependencies.koinTestDependencies
import gradle.dependencies.kotlinReflectDependencies
import gradle.dependencies.loadLocalProjects
import gradle.dependencies.publishingToS3

plugins {
    id("app.kotlin-library-conventions")
}

loadLocalProjects("api", listOf(":koin:koin-core"))

kotlinReflectDependencies("api")
koinTestDependencies("api")
junitDependencies("api")

publishing {
    publishingToS3(this@publishing)
}
