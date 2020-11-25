package de.codecentric.vertx.koin.webclient.properties

import de.codecentric.vertx.koin.core.properties.ApplicationProperties
import de.codecentric.vertx.koin.core.properties.ProfileProperties
import io.vertx.ext.web.client.WebClientOptions

data class WebClientApplicationProperties(
    override var applicationName: String,
    override var activeProfile: String,
    override var profiles: List<WebClientProfileProperties>
) : ApplicationProperties<WebClientProfileProperties>

data class WebClientProfileProperties(
    override var profile: String = "default",
    var webClientOptions: WebClientOptions = WebClientOptions()
) : ProfileProperties
