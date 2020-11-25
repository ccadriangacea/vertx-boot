plugins {
    val kotlinDslVersion: String by System.getProperties()
    id("org.gradle.kotlin.kotlin-dsl") version kotlinDslVersion

    val kotlinVersion: String by System.getProperties()
    kotlin("jvm") version kotlinVersion apply false
    // kotlin("kapt") version kotlinVersion apply false
}

kotlinDslPluginOptions {
    experimentalWarning.set(false)
}

repositories {
    mavenLocal()
    mavenCentral()
    jcenter()
}

dependencies {
    val kotlinVersion: String by System.getProperties()
    implementation(platform("org.jetbrains.kotlin:kotlin-bom:$kotlinVersion"))

    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
}
