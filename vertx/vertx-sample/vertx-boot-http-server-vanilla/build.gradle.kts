import gradle.dependencies.loadLocalProjects

loadLocalProjects("implementation", listOf(":vertx:vertx-boot:vertx-boot-http-server"))

application {
    mainClass.set("boot.vertx.httpserver.app.VanillaVertxBootHttpServerAppLauncher")
}