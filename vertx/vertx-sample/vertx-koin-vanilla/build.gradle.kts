import gradle.dependencies.loadLocalProjects

loadLocalProjects("implementation", listOf(":vertx:vertx-koin"))

application {
    mainClass.set("vertx.koin.app.VanillaVertxKoinAppLauncher")
}