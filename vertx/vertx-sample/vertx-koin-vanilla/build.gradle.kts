import gradle.dependencies.loadLocalProjects

plugins {
    id("common.kotlin-application")
}

loadLocalProjects("implementation", listOf(":vertx:vertx-koin:vertx-koin-core"))

application {
    mainClass.set("vertx.koin.app.VanillaVertxKoinAppLauncher")
}