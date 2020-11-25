import gradle.dependencies.loadLocalProjects

loadLocalProjects("implementation", listOf(":vertx:vertx-boot:vertx-boot-instance"))

application {
    mainClass.set("boot.vertx.instance.app.VanillaVertxBootAppLauncher")
}
