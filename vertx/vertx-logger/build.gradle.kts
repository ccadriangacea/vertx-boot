import gradle.dependencies.kotlinLoggingDependencies
import gradle.dependencies.publishingToS3

plugins {
    `java-library`

    id("cloud.maven-publishing")
}

kotlinLoggingDependencies("api")

publishing {
    publishingToS3(this@publishing)
}
