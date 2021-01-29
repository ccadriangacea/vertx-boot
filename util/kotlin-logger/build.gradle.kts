import gradle.dependencies.CoreVersions.Logger
import gradle.dependencies.CoreVersions.Kotlin
import gradle.dependencies.import
import gradle.util.publishingToS3

plugins {
    id("common.kotlin-library")
}

dependencies {
    "api"("org.apache.logging.log4j:log4j-slf4j-impl:${Logger.log4j}")
    "api"("io.github.microutils:kotlin-logging:${Logger.kotlinLogging}")

    Kotlin.reflect.import("testImplementation", this)
}

publishing {
    publishingToS3(this@publishing)
}
