import gradle.dependencies.loadLocalProjects

plugins {
    id("app.kotlin-application-conventions")
}

loadLocalProjects("implementation", listOf(":vertx:vertx-boot:vertx-boot-instance"))

application {
    mainClass.set("boot.vertx.instance.app.VanillaVertxBootAppLauncher")
}
