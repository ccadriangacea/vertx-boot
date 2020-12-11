import gradle.dependencies.kotlinLoggingDependencies
import gradle.dependencies.publishingToS3

 plugins {
    id("app.kotlin-library-conventions")
}

kotlinLoggingDependencies("api")

publishing {
    publishingToS3(this@publishing)
}
