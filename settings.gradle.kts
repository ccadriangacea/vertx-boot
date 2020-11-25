import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

rootProject.name = "vertx-boot"

private val pathSeparator: String by System.getProperties()
private val gradlePathSeparator: String by System.getProperties()

pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        jcenter()
        gradlePluginPortal()
        maven { url = uri("https://plugins.gradle.org/m2/") }
    }
}

"vertx".loadGradleFileStructure(
    listOf(
        "vertx",
        "vertx:vertx-boot:vertx-boot-gcp-run",
        "vertx:vertx-boot:vertx-boot-http-server",
        "vertx:vertx-boot:vertx-boot-instance",
        "vertx:vertx-common",
        "vertx:vertx-gcp-koin:vertx-firebase-auth-admin-koin",
        "vertx:vertx-gcp-koin:vertx-firebase-auth-idtoken-koin",
        "vertx:vertx-gcp-koin:vertx-gcp-core-koin",
        "vertx:vertx-gcp-koin:vertx-gcp-pubsub-koin",
        "vertx:vertx-gcp-koin:vertx-gcp-run-koin",
        "vertx:vertx-gcp-koin:vertx-gcp-tasks-koin",
        "vertx:vertx-koin",
        "vertx:vertx-logger",
        "vertx:vertx-sample:vertx-boot-http-server-vanilla",
        "vertx:vertx-sample:vertx-boot-vanilla",
        "vertx:vertx-sample:vertx-koin-vanilla",
        "vertx:vertx-sample:vertx-vanilla",
        "vertx:vertx-sample:vertx-web-koin-vanilla",
        "vertx:vertx-service-discovery-koin",
        "vertx:vertx-test-koin",
        "vertx:vertx-web-koin",
        "vertx:vertx-webclient-koin"
    )
)

// include subdirectories as projects
fun String.loadGradleFileStructure(subprojects: List<String>) {
    val rootProjectPath = rootDir.absolutePath.toString()
    Paths.get(rootProject.projectDir.absolutePath, this)
        .let { toLoadProjectPath ->
            logger.lifecycle("  > Loading: loadGradleFileStructure for project $toLoadProjectPath")
            Files.walk(toLoadProjectPath, 2)
                .filter { it.toFile().isDirectory }
                .forEach { subprojectPath ->
                    absolutePathToGradlePath(subprojectPath, rootProjectPath)
                        .apply {
                            val projectName = this.removePrefix(":")
                            if (subprojects.contains(projectName)) {
                                logger.debug("    > Subproject path: $subprojectPath")
                                logger.debug("      > Subproject: $projectName")
                                logger.debug("      > Including project: $projectName")

                                include(projectName)
                                findProject(this)?.let { project ->
                                    project.name = projectName.substringAfterLast(":")
                                    project.projectDir = subprojectPath.toFile()
                                }
                            }
                        }
                }
        }
}

fun absolutePathToGradlePath(absolutePath: Path, removePrefix: String): String = absolutePath
    .toAbsolutePath().toString()
    .removePrefix(removePrefix)
    .replace(pathSeparator, gradlePathSeparator)