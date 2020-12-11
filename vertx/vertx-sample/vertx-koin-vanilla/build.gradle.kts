import gradle.dependencies.loadLocalProjects

plugins {
    id("app.kotlin-application-conventions")
}

loadLocalProjects("implementation", listOf(":vertx:vertx-koin:vertx-koin-core"))

application {
    mainClass.set("vertx.koin.app.VanillaVertxKoinAppLauncher")
}