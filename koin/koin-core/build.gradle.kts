import gradle.dependencies.koinDependencies
import gradle.dependencies.publishingToS3

plugins {
    id("app.kotlin-library-conventions")
}

koinDependencies("api")

publishing {
    publishingToS3(this@publishing)
}
