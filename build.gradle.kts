logger.lifecycle("\n>>> Running build.gradle.kts in vertx-boot\n")

plugins {
    idea
}

allprojects {
    repositories {
        mavenLocal()
        mavenCentral()
        jcenter()
    }
}