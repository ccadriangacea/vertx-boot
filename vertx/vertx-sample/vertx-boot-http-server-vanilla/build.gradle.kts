import gradle.dependencies.loadLocalProjects

plugins {
    id("app.kotlin-application-conventions")
}

loadLocalProjects("implementation", listOf(":vertx:vertx-boot:vertx-boot-http-server"))

application {
    mainClass.set("boot.vertx.httpserver.app.VanillaVertxBootHttpServerAppLauncher")
}