@file:Suppress("unused", "TooManyFunctions")

package gradle.dependencies

import org.gradle.api.Project
import org.gradle.api.credentials.AwsCredentials
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.credentials
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.provideDelegate

fun Project.publishingToS3(delegate: PublishingExtension) {
    val awsAccessKey by System.getProperties()
    val awsSecretKey by System.getProperties()

    val groupId = this@publishingToS3.group.toString()
    val artifactId = this@publishingToS3.name
    val version = this@publishingToS3.version.toString()

    delegate.publications {
        create<MavenPublication>("s3-maven-repository") {
            this.groupId = groupId
            this.artifactId = artifactId
            this.version = version

            from(components["java"])
        }
    }

    delegate.repositories {
        maven {
            if (version.endsWith("SNAPSHOT")) setUrl("s3://s3-maven-repository/snapshot")
            else setUrl("s3://s3-maven-repository/release")

            credentials(AwsCredentials::class) {
                accessKey = awsAccessKey.toString()
                secretKey = awsSecretKey.toString()
            }
        }
    }
}
