import gradle.dependencies.loadLocalProjects

plugins {
    id("app.kotlin-application-conventions")
}

loadLocalProjects("implementation", listOf(":vertx:vertx-common"))

application {
    mainClass.set("vertx.app.VertxVanilla")
}
