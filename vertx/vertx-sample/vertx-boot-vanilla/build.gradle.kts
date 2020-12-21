import gradle.util.loadLocalProjects

plugins {
    id("common.kotlin-application")
}

loadLocalProjects("implementation", listOf(":vertx:vertx-boot:vertx-boot-instance"))

application {
    mainClass.set("boot.vertx.instance.app.VanillaVertxBootAppLauncher")
}
