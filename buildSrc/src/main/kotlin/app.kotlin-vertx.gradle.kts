dependencies {
    val vertXVersion: String by System.getProperties()

    "implementation"("io.vertx:vertx-core:$vertXVersion")
    "implementation"("io.vertx:vertx-lang-kotlin:$vertXVersion")
    "implementation"("io.vertx:vertx-lang-kotlin-coroutines:$vertXVersion")

    // Testing Vert.x
    "testImplementation"("io.vertx:vertx-junit5:$vertXVersion")
}