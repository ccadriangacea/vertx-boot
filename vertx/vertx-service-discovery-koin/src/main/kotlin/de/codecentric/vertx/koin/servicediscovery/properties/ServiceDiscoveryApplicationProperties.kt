package de.codecentric.vertx.koin.servicediscovery.properties

import de.codecentric.vertx.koin.core.properties.ApplicationProperties
import de.codecentric.vertx.koin.core.properties.ProfileProperties
import de.codecentric.vertx.koin.servicediscovery.module.VertxServiceDiscoveryCommonModule.defaultServiceDiscoveryOptions
import io.vertx.servicediscovery.ServiceDiscoveryOptions

data class ServiceDiscoveryApplicationProperties(
    override var applicationName: String,
    override var activeProfile: String,
    override var profiles: List<ServiceDiscoveryProfileProperties>
) : ApplicationProperties<ServiceDiscoveryProfileProperties>

data class ServiceDiscoveryProfileProperties(
    override var profile: String = "default",
    var serviceDiscoveryOptions: ServiceDiscoveryOptions = defaultServiceDiscoveryOptions()
) : ProfileProperties
