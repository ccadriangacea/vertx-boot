logger.lifecycle("\n>>> Running settings.gradle in buildSrc\n")

bootstrapProperties()

rootProject.name = "buildSrc"

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://plugins.gradle.org/m2/")
    }
}

fun bootstrapProperties() {
    val rootProjectPath = rootDir.parentFile.absolutePath
    logger.lifecycle("  > bootstrapping gradle.properties files from root project: $rootProjectPath")
    org.gradle.util.GUtil.loadProperties(file("$rootProjectPath/gradle.properties"))
        .apply {
            filter { it.key.toString().startsWith("systemProp.") }
                .forEach {
                    logger.debug("    + adding property: $it")
                    it.key.toString().replace("systemProp.", "")
                        .apply { System.getProperties()[this] = it.value }
                }
        }
    logger.lifecycle("  < done loading properties from root gradle.properties...")
    logger.debug("  > System properties: ")
        .also { System.getProperties().map { logger.debug("    - ${it.key} -> ${it.value}") } }
}