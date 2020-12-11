package de.codecentric.vertx.mongo.properties

import de.codecentric.vertx.koin.core.properties.ApplicationProperties
import de.codecentric.vertx.koin.core.properties.ProfileProperties

data class MongodbApplicationProperties(
    override var applicationName: String,
    override var activeProfile: String,
    override var profiles: List<MongodbProfileProperties>
) : ApplicationProperties<MongodbProfileProperties>

data class MongodbProfileProperties(
    override var profile: String = "default",
    var mongodbClientOptions: Map<String, Any> = emptyMap(),
) : ProfileProperties
