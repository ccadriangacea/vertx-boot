package de.codecentric.vertx.koin.core.properties

data class VertxApplicationProperties(
    override val applicationName: String,
    override val activeProfile: String,
    override val profiles: List<VertxProfileProperties>
) : ApplicationProperties<VertxProfileProperties>

data class VertxProfileProperties(
    override val profile: String,
    var mainVerticleOptions: Map<String, Any> = emptyMap()
) : ProfileProperties

enum class MainVerticleOptionKeys(val propertyKey: String) {
    CREATE_DATABASE_AT_START("createDatabaseAtStart"),
    IMPORT_DATA_AT_START("importDataAtStart")
}