import gradle.dependencies.koinDependencies
import gradle.util.publishingToS3

plugins {
    id("common.kotlin-library")
}

koinDependencies("api")

publishing {
    publishingToS3(this@publishing)
}
