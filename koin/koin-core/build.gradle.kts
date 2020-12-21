import gradle.dependencies.Versions
import gradle.util.publishingToS3

plugins {
    id("common.kotlin-library")
}

dependencies {
    "api"(Versions.Koin.coreDependencies)
}

publishing {
    publishingToS3(this@publishing)
}
