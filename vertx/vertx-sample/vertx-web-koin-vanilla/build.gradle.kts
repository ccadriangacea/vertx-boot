import gradle.dependencies.loadLocalProjects

loadLocalProjects("implementation", listOf(":vertx:vertx-web-koin"))

application {
    mainClass.set("vertx.web.koin.app.VanillaVertxWebKoinAppLauncher")
}
