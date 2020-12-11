package de.codecentric.vertx.koin.web.properties

import de.codecentric.vertx.koin.core.properties.ApplicationProperties
import de.codecentric.vertx.koin.core.properties.ProfileProperties
import io.vertx.core.http.HttpServerOptions

data class HttpServerApplicationProperties(
    override var applicationName: String,
    override var activeProfile: String,
    override var profiles: List<HttpServerProfileProperties>
) : ApplicationProperties<HttpServerProfileProperties>

data class HttpServerProfileProperties(
    override var profile: String = "default",
    var defaultPort: Int = 8080,
    var httpServerOptions: Map<String, HttpServerOptions> = emptyMap(),
    var securityOptions: Map<String, String> = emptyMap()
) : ProfileProperties
