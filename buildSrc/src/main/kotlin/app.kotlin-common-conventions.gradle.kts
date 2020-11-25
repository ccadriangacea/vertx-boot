import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    // kotlin("kapt")
}

val kotlinVersion: String by System.getProperties()
val coroutinesVersion: String by System.getProperties()

val jvmTargetVersion: String by System.getProperties()
val kotlinApiVersion: String by System.getProperties()
val nettyVersion: String by System.getProperties()

configurations.all {
    exclude("org.slf4j", "slf4j-log4j12")

    exclude(group = "junit", module = "junit")
    exclude(group = "org.junit.vintage", module = "junit-vintage-engine")

    resolutionStrategy {
        this.eachDependency {
            when (requested.group) {
                "org.jetbrains.kotlin" -> {
                    useTarget(mapOf("group" to requested.group, "name" to requested.name, "version" to kotlinVersion))
                    because("Kotlin version $kotlinVersion is latest")
                }
                "org.jetbrains.kotlinx" -> {
                    if (requested.name.startsWith("kotlinx-coroutines")) {
                        useTarget(mapOf("group" to requested.group, "name" to requested.name, "version" to coroutinesVersion))
                        because("latest thingy everywhere")
                    }
                }
            }
        }
    }
}

repositories {
    mavenLocal()
    mavenCentral()
    jcenter()
}

dependencies {
    // assure same version is loaded
    implementation(platform("org.jetbrains.kotlin:kotlin-bom:$kotlinVersion"))
    implementation(platform("io.netty:netty-bom:$nettyVersion"))

    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    // implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:$coroutinesVersion")

    // Testing
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:$kotlinVersion")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutinesVersion")

    // Junit
    val junitVersion: String by System.getProperties()
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")

    // AspectJ
    val aspectJVersion: String by System.getProperties()
    testImplementation("org.assertj:assertj-core:$aspectJVersion")
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = jvmTargetVersion
    apiVersion = kotlinApiVersion
    languageVersion = kotlinApiVersion
    javaParameters = true
    freeCompilerArgs = freeCompilerArgs + "-Xopt-in=kotlin.RequiresOptIn"
}

val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = jvmTargetVersion
    apiVersion = kotlinApiVersion
    languageVersion = kotlinApiVersion
    javaParameters = true
    freeCompilerArgs = freeCompilerArgs + "-Xopt-in=kotlin.RequiresOptIn"
}
