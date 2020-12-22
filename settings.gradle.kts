import java.nio.file.Files
import java.nio.file.Paths

logger.lifecycle("\n>>> Running settings.gradle.kts in vertx-boot\n")

rootProject.name = "vertx-boot"

pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        jcenter()
        gradlePluginPortal()
        maven { url = uri("https://plugins.gradle.org/m2/") }
    }
}

"koin".loadGradleFileStructure(
    listOf(
        "koin:koin-core",
        "koin:koin-test"
    )
)

"util".loadGradleFileStructure(
    listOf(
        "util:kotlin-logger",
        "util:fn-result"
    )
)

"vertx".loadGradleFileStructure(
    listOf(
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

        "vertx:vertx-koin:vertx-koin-core",
        "vertx:vertx-koin:vertx-mongodb-koin",
        "vertx:vertx-koin:vertx-web-koin",
        "vertx:vertx-koin:vertx-webclient-koin",
        "vertx:vertx-koin:vertx-service-discovery-koin",

        "vertx:vertx-sample:vertx-boot-http-server-vanilla",
        "vertx:vertx-sample:vertx-boot-vanilla",
        "vertx:vertx-sample:vertx-koin-vanilla",
        "vertx:vertx-sample:vertx-vanilla",
        "vertx:vertx-sample:vertx-web-koin-vanilla",

        "vertx:vertx-test-koin"
    )
)

// include subdirectories as projects
fun String.loadGradleFileStructure(toLoadSubprojects: List<String>, maxDepth: Int = 2, removeSubmodulePath: String = "") {
    val rootProjectPath = rootDir.absolutePath.toString()
    logger.lifecycle("  > Loading subprojects: $toLoadSubprojects in rootProject: $rootProjectPath")

    val toLoadProjectPath = Paths.get(rootProject.projectDir.absolutePath, this)
    Files.walk(toLoadProjectPath, maxDepth)
        .filter { it.toFile().isDirectory }
        .forEach { subprojectPath ->
            val subprojectPathProject = subprojectPath.toAbsolutePath().toString()
                .removePrefix(rootProjectPath)
                .replace(File.separator, ":")
            val projectName = subprojectPathProject.removePrefix(":$removeSubmodulePath")
            if (toLoadSubprojects.contains(projectName)) {
                include(projectName)
                findProject(":$projectName")?.let { project ->
                    project.name = projectName.substringAfterLast(":")
                    project.projectDir = subprojectPath.toFile()

                    logger.info("    < Subproject name: ${project.name}")
                    logger.info("                 path: ${project.projectDir}")
                }
            }
        }
}
