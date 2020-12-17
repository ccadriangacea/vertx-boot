import gradle.util.publishingToS3

plugins {
    id("common.kotlin-library")
    id("app.kotlin-vertx")
}

publishing {
    publishingToS3(this@publishing)
}