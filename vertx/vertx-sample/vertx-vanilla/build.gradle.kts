import gradle.dependencies.loadLocalProjects

plugins {
    id("common.kotlin-application")
}

loadLocalProjects("implementation", listOf(":vertx:vertx-common"))

application {
    mainClass.set("vertx.app.VertxVanilla")
}
