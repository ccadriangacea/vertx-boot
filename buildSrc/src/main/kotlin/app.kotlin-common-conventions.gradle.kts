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

repositories {
    mavenLocal()
    mavenCentral()
    jcenter()
}

kotlin {
    sourceSets {
        val main by getting {
            dependencies {
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
                api("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:$coroutinesVersion")
            }
        }
        logger.debug("Loaded sourceSets dependencies: $main")
        val test by getting {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-test-junit5:$kotlinVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutinesVersion")
            }
        }
        logger.debug("Loaded sourceSets dependencies: $test")
    }
}

dependencies {
    // assure same version is loaded
    "implementation"(platform("org.jetbrains.kotlin:kotlin-bom"))
    "implementation"(platform("io.netty:netty-bom:$nettyVersion"))

    // Junit
    val junitVersion: String by System.getProperties()
    "testImplementation"("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    "testImplementation"("org.junit.jupiter:junit-jupiter-params:$junitVersion")
    "testRuntimeOnly"("org.junit.jupiter:junit-jupiter-engine:$junitVersion")

    // AspectJ
    val aspectJVersion: String by System.getProperties()
    "testImplementation"("org.assertj:assertj-core:$aspectJVersion")
}

tasks {
    withType<KotlinCompile>()
        .configureEach {
            kotlinOptions {
                jvmTarget = jvmTargetVersion
                apiVersion = kotlinApiVersion
                languageVersion = kotlinApiVersion
                javaParameters = true
                freeCompilerArgs = freeCompilerArgs + "-Xopt-in=kotlin.RequiresOptIn"
            }
        }

    withType(Test::class) {
        useJUnitPlatform()
        exclude("**/Native*")
    }
}
