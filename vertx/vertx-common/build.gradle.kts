import gradle.dependencies.publishingToS3
import gradle.dependencies.vertxCoreDependencies
import gradle.dependencies.vertxKotlinDependencies

plugins {
    `java-library`

    id("cloud.maven-publishing")
}

vertxCoreDependencies("api")
vertxKotlinDependencies("api")

publishing {
    publishingToS3(this@publishing)
}