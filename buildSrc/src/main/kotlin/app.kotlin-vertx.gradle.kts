dependencies {
    val vertXVersion: String by System.getProperties()

    "api"("io.vertx:vertx-core:$vertXVersion")
    "api"("io.vertx:vertx-lang-kotlin:$vertXVersion")
    "api"("io.vertx:vertx-lang-kotlin-coroutines:$vertXVersion")

    // Testing Vert.x
    "testImplementation"("io.vertx:vertx-junit5:$vertXVersion")
}