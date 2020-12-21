logger.lifecycle("\n>>> Running build.gradle.kts in vertx-boot\n")

allprojects {
    repositories {
        mavenLocal()
        mavenCentral()
        jcenter()
    }
}