@file:Suppress("unused", "TooManyFunctions")

package gradle.dependencies

import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.kotlin
import org.gradle.kotlin.dsl.project
import org.gradle.kotlin.dsl.provideDelegate

val vertXVersion: String by System.getProperties()

fun Project.kotlinDependencies(configurationName: String = "implementation") {
    val kotlinVersion: String by System.getProperties()
    val coroutinesVersion: String by System.getProperties()

    dependencies {
        configurationName(kotlin(module = "stdlib-jdk8", version = kotlinVersion))
        configurationName("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
        configurationName("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:$coroutinesVersion")
    }
}

fun Project.kotlinReflectDependencies(configurationName: String = "implementation") {
    val kotlinVersion: String by System.getProperties()

    dependencies {
        configurationName("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    }
}

fun Project.kotlinTestingDependencies(configurationName: String = "testImplementation") {
    val coroutinesVersion: String by System.getProperties()

    dependencies {
        configurationName("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutinesVersion")
    }
}

fun Project.vertxCoreDependencies(configurationName: String = "api") {
    dependencies {
        configurationName("io.vertx:vertx-core:$vertXVersion")
    }
}

fun Project.vertxKotlinDependencies(configurationName: String = "api") {
    dependencies {
        configurationName("io.vertx:vertx-lang-kotlin:$vertXVersion")
        configurationName("io.vertx:vertx-lang-kotlin-coroutines:$vertXVersion")
    }
}

fun Project.vertxWebDependencies(configurationName: String = "api") {
    dependencies {
        configurationName("io.vertx:vertx-web:$vertXVersion")
    }
}

fun Project.vertxJdbcDependencies(configurationName: String = "api") {
    dependencies {
        configurationName("io.vertx:vertx-auth-jdbc:$vertXVersion")
    }
}

fun Project.vertxJwtDependencies(configurationName: String = "api") {
    dependencies {
        configurationName("io.vertx:vertx-auth-jwt:$vertXVersion")
    }
}

fun Project.vertxAuthDependencies(configurationName: String = "api") {
    dependencies {
        configurationName("io.vertx:vertx-auth-common:$vertXVersion")
    }
}

fun Project.vertxOauth2Dependencies(configurationName: String = "api") {
    dependencies {
        configurationName("io.vertx:vertx-auth-oauth2:$vertXVersion")
    }
}

fun Project.vertxConfigDependencies(configurationName: String = "api") {
    dependencies {
        configurationName("io.vertx:vertx-config:$vertXVersion")
    }
}

fun Project.vertxWebClientDependencies(configurationName: String = "api") {
    dependencies {
        configurationName("io.vertx:vertx-web-client:$vertXVersion")
    }
}

fun Project.vertxPgClientDependencies(configurationName: String = "implementation") {
    dependencies {
        configurationName("io.vertx:vertx-pg-client:$vertXVersion")
    }
}

fun Project.vertxJooqDependencies(configurationName: String = "implementation") {
    val vertxJooqVersion: String by System.getProperties()

    dependencies {
        configurationName("io.github.jklingsporn:vertx-jooq-classic-reactive:$vertxJooqVersion")
        configurationName("io.github.jklingsporn:vertx-jooq-generate:$vertxJooqVersion")
    }
}

fun Project.postgresDependencies(configurationName: String = "api") {
    val postgresVersion: String by System.getProperties()

    dependencies {
        configurationName("org.postgresql:postgresql:$postgresVersion")
    }
}

fun Project.vertxHazelcastDependencies(configurationName: String = "api") {
    val hazelcastKubernetesVersion: String by System.getProperties()

    dependencies {
        configurationName("io.vertx:vertx-hazelcast:$vertXVersion")
        configurationName("com.hazelcast:hazelcast-kubernetes:$hazelcastKubernetesVersion")
    }
}

fun Project.vertxIgniteDependencies(configurationName: String = "api") {
    val igniteVersion: String by System.getProperties()

    dependencies {
        configurationName("io.vertx:vertx-ignite:$vertXVersion")
        configurationName("org.apache.ignite:ignite-slf4j:$igniteVersion")
    }
}

fun Project.vertxZookeeperDependencies(configurationName: String = "api") {
    dependencies {
        // TODO Fix No release for current milestone
        configurationName("io.vertx:vertx-zookeeper:4.0.0-milestone4")
    }
}

fun Project.vertxServiceDiscoveryDependencies(configurationName: String = "api") {
    dependencies {
        configurationName("io.vertx:vertx-service-discovery:$vertXVersion")
    }
}

fun Project.vertxCodeGenDependencies() {
    // kapt dependency are not delivered as api
    // do not forget to include the kapt plugin: kotlin("kapt")
    dependencies {
        "implementation"("io.vertx:vertx-codegen:$vertXVersion")
        "kapt"("io.vertx:vertx-codegen:$vertXVersion")
    }
}

fun Project.vertxCodeGenAndServiceProxyDiscoveryDependencies(configurationName: String = "api") {
    dependencies {
        "implementation"("io.vertx:vertx-codegen:$vertXVersion")
        "kapt"("io.vertx:vertx-codegen:$vertXVersion")

        "implementation"("io.vertx:vertx-service-proxy:$vertXVersion")
        "kapt"("io.vertx:vertx-service-proxy:$vertXVersion")

        configurationName("io.vertx:vertx-service-discovery:$vertXVersion")
    }
}

fun Project.vertxRabbitMqDependencies(configurationName: String = "api") {
    dependencies {
        configurationName("io.vertx:vertx-rabbitmq-client:$vertXVersion")
    }
}

fun Project.koinDependencies(configurationName: String = "api") {
    val koinVersion: String by System.getProperties()

    dependencies {
        configurationName("org.koin:koin-core:$koinVersion")
    }
}

fun Project.kotlinLoggingDependencies(configurationName: String = "api") {
    val log4jVersion: String by System.getProperties()
    val kotlinLoggingVersion: String by System.getProperties()

    dependencies {
        configurationName("org.apache.logging.log4j:log4j-slf4j-impl:$log4jVersion")
        configurationName("io.github.microutils:kotlin-logging:$kotlinLoggingVersion")
    }
}

fun Project.jsonDependencies(configurationName: String = "api") {
    val jsonVersion: String by System.getProperties()

    dependencies {
        configurationName("com.fasterxml.jackson.module:jackson-module-kotlin:$jsonVersion")
        configurationName("com.fasterxml.jackson.module:jackson-module-paranamer:$jsonVersion")
    }
}

// TESTING
fun Project.junitDependencies(configurationName: String = "testImplementation") {
    val kotlinVersion: String by System.getProperties()
    val junitVersion: String by System.getProperties()
    val aspectJVersion: String by System.getProperties()

    dependencies {
        configurationName("org.junit.jupiter:junit-jupiter-api:$junitVersion")
        configurationName("org.junit.jupiter:junit-jupiter-params:$junitVersion")

        "testRuntimeOnly"("org.junit.jupiter:junit-jupiter-engine:$junitVersion")

        configurationName("org.jetbrains.kotlin:kotlin-test-junit5:$kotlinVersion")

        configurationName("org.assertj:assertj-core:$aspectJVersion")
    }
}

fun Project.vertxJunitDependencies(configurationName: String = "testImplementation") {
    dependencies {
        configurationName("io.vertx:vertx-junit5:$vertXVersion")
        configurationName("io.reactiverse:reactiverse-junit5-web-client:$vertXVersion")
    }
}

fun Project.koinTestDependencies(configurationName: String = "testImplementation") {
    val koinVersion: String by System.getProperties()

    dependencies {
        configurationName("org.koin:koin-test:$koinVersion")
    }
}

fun Project.koinVertxTestDependencies(configurationName: String = "testImplementation") {
    val koinVersion: String by System.getProperties()

    dependencies {
        configurationName("org.koin:koin-test:$koinVersion")

        configurationName("io.vertx:vertx-junit5:$vertXVersion")

        /*
         * configurationName("io.vertx:vertx-web-client:$vertXVersion")
         * configurationName("io.reactiverse:reactiverse-junit5-web-client:$vertXVersion")
         */
    }
}

// HELPERS
fun Project.loadLocalProjects(configuration: String = "api", projectNames: List<String>) {
    dependencies {
        projectNames.stream().forEach { configuration(project(it)) }
    }
}
