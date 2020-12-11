plugins {
    val kotlinDslVersion: String by System.getProperties()
    id("org.gradle.kotlin.kotlin-dsl") version kotlinDslVersion
}

kotlinDslPluginOptions {
    experimentalWarning.set(false)
}

allprojects {
    repositories {
        mavenLocal()
        mavenCentral()
        jcenter()
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin")
}
