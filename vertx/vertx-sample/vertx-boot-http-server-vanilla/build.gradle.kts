import gradle.util.loadLocalProjects

plugins {
    id("common.kotlin-application")
}

loadLocalProjects("implementation", listOf(":vertx:vertx-boot:vertx-boot-http-server"))

application {
    mainClass.set("boot.vertx.httpserver.app.VanillaVertxBootHttpServerAppLauncher")
}