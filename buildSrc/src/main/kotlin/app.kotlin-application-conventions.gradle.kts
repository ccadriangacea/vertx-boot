plugins {
    application

    id("app.kotlin-common-conventions")
}

tasks {
    findByName("clean")
        .apply { this?.doLast { delete(project.projectDir.toPath().resolve("out").toFile().absolutePath) } }

    withType(Test::class) {
        useJUnitPlatform()
    }
}
