package gradle.dependencies

import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.provideDelegate

fun Project.gcpAuthDependencies(configurationName: String = "api") {
    val gcloudAuthVersion: String by System.getProperties()

    dependencies {
        configurationName("com.google.auth:google-auth-library-oauth2-http:$gcloudAuthVersion")
    }
}

fun Project.gcpGaxDependencies(configurationName: String = "api") {
    val gcloudGaxVersion: String by System.getProperties()

    dependencies {
        configurationName("com.google.api:gax:$gcloudGaxVersion")
    }
}

fun Project.gcpBomDependencies(configurationName: String = "api") {
    val gcloudLibrariesBomVersion: String by System.getProperties()

    dependencies {
        configurationName(platform("com.google.cloud:libraries-bom:$gcloudLibrariesBomVersion"))
    }
}

fun Project.gcpResourceManagerDependencies(configurationName: String = "api") {
    dependencies {
        configurationName("com.google.cloud:google-cloud-resourcemanager")
    }
}

fun Project.gcpPubSubDependencies(configurationName: String = "api") {
    val gcloudPubSubVersion: String by System.getProperties()

    dependencies {
        configurationName("com.google.cloud:google-cloud-pubsub:$gcloudPubSubVersion")
    }
}

fun Project.gcpFirebaseAdminDependencies(configurationName: String = "api") {
    val firebaseAdminVersion: String by System.getProperties()

    dependencies {
        configurationName("com.google.firebase:firebase-admin:$firebaseAdminVersion")
    }
}

fun Project.gcpTasksDependencies(configurationName: String = "api") {
    val gcloudTasksVersion: String by System.getProperties()

    dependencies {
        configurationName("com.google.cloud:google-cloud-tasks:$gcloudTasksVersion")
    }
}