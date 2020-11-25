plugins {
    val benManesVersionsPluginVersion: String by System.getProperties()
    id("com.github.ben-manes.versions") version benManesVersionsPluginVersion apply false

    val shadowPluginVersion: String by System.getProperties()
    id("com.github.johnrengelman.shadow") version shadowPluginVersion apply false
}

allprojects {
    repositories {
        mavenLocal()
        mavenCentral()
        jcenter()
    }
}

subprojects {
    apply(plugin = "app.kotlin-application-conventions")

    apply(plugin = "com.github.ben-manes.versions")
}
