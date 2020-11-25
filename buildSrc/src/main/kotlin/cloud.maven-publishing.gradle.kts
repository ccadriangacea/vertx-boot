plugins {
    `maven-publish`
}

val projectVertxBootVersion: String by System.getProperties()

group = "de.codecentric"
version = projectVertxBootVersion