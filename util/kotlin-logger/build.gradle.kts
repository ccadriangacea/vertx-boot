import gradle.dependencies.kotlinLoggingDependencies
import gradle.util.publishingToS3

 plugins {
     id("common.kotlin-library")
}

kotlinLoggingDependencies("api")

publishing {
    publishingToS3(this@publishing)
}
