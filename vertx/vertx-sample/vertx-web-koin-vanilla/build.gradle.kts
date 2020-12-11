import gradle.dependencies.loadLocalProjects

plugins {
    id("app.kotlin-application-conventions")
}

loadLocalProjects("implementation", listOf(":vertx:vertx-koin:vertx-web-koin"))

application {
    mainClass.set("vertx.web.koin.app.VanillaVertxWebKoinAppLauncher")
}
