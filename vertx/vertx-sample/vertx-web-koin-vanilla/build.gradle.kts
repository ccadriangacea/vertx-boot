import gradle.dependencies.loadLocalProjects

plugins {
    id("common.kotlin-application")
}

loadLocalProjects("implementation", listOf(":vertx:vertx-koin:vertx-web-koin"))

application {
    mainClass.set("vertx.web.koin.app.VanillaVertxWebKoinAppLauncher")
}
