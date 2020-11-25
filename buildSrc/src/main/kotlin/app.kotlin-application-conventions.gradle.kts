plugins {
    application
    idea

    id("app.kotlin-common-conventions")
}

tasks {
    val gradleWrapperVersion: String by System.getProperties()
    withType<Wrapper> {
        distributionType = Wrapper.DistributionType.BIN
        gradleVersion = gradleWrapperVersion
    }
    findByName("build")?.dependsOn(Wrapper::class)

    findByName("clean").apply {
        this?.doLast {
            delete(project.projectDir.absolutePath.plus("/out"))
        }
    }

    withType(Test::class) {
        useJUnitPlatform()
    }
}