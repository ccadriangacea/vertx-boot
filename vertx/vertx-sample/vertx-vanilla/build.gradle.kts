import gradle.dependencies.loadLocalProjects

loadLocalProjects("implementation", listOf(":vertx:vertx-common"))

application {
    mainClass.set("vertx.app.VertxVanilla")
}
