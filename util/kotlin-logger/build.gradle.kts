import gradle.dependencies.Versions
import gradle.util.publishingToS3

plugins {
    id("common.kotlin-library")
}

dependencies {
    "api"("org.apache.logging.log4j:log4j-slf4j-impl:${Versions.Logger.log4j}")
    "api"("io.github.microutils:kotlin-logging:${Versions.Logger.kotlinLogging}")
}

publishing {
    publishingToS3(this@publishing)
}
