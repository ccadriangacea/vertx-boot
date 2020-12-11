import gradle.dependencies.publishingToS3

plugins {
    id("app.kotlin-library-conventions")
    id("app.kotlin-vertx")
}

publishing {
    publishingToS3(this@publishing)
}