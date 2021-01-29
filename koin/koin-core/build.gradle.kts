import gradle.dependencies.CoreVersions.Koin
import gradle.dependencies.import
import gradle.util.publishingToS3

plugins {
    id("common.kotlin-library")
}

dependencies {
    Koin.coreDependencies.import("api", this)
}

publishing {
    publishingToS3(this@publishing)
}
