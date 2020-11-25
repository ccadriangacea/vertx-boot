package de.codecentric.vertx.koin.servicediscovery.module

import de.codecentric.vertx.common.fn.FnResult
import de.codecentric.vertx.common.fn.getResult
import de.codecentric.vertx.common.fn.map
import de.codecentric.vertx.common.fn.onFailure
import de.codecentric.vertx.koin.core.ModuleWithOrder
import de.codecentric.vertx.koin.core.module.KoinModule
import de.codecentric.vertx.koin.core.module.VertxConfigCommonModule.getActiveProfileProperty
import de.codecentric.vertx.koin.core.module.VertxConfigKoinQualifiers.VERTX_CONFIG_RETRIEVER
import de.codecentric.vertx.koin.core.module.VertxKoinQualifiers.VERTX_INSTANCE
import de.codecentric.vertx.koin.core.module.qualifier
import de.codecentric.vertx.koin.core.toModuleWithOrder
import de.codecentric.vertx.koin.servicediscovery.module.VertServiceDiscoveryKoinQualifiers.VERTX_SERVICE_DISCOVERY
import de.codecentric.vertx.koin.servicediscovery.module.VertServiceDiscoveryKoinQualifiers.VERTX_SERVICE_DISCOVERY_OPTIONS
import de.codecentric.vertx.koin.servicediscovery.module.VertxServiceDiscoveryCommonModule.getServiceDiscovery
import de.codecentric.vertx.koin.servicediscovery.module.VertxServiceDiscoveryCommonModule.getServiceDiscoveryOptions
import de.codecentric.vertx.koin.servicediscovery.properties.ServiceDiscoveryApplicationProperties
import de.codecentric.vertx.koin.servicediscovery.properties.ServiceDiscoveryProfileProperties
import io.vertx.config.ConfigRetriever
import io.vertx.core.AsyncResult
import io.vertx.core.Future.succeededFuture
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.servicediscovery.ServiceDiscovery
import io.vertx.servicediscovery.ServiceDiscoveryOptions
import org.koin.core.qualifier.StringQualifier
import org.koin.dsl.module

class VertxServiceDiscoveryKoinModule : KoinModule {
    private val vertxServiceDiscoveryOrderedKoinModule = module {
        factory(VERTX_SERVICE_DISCOVERY_OPTIONS.qualifier) {
            val configRetriever: ConfigRetriever = get(VERTX_CONFIG_RETRIEVER.qualifier)

            getServiceDiscoveryOptions(configRetriever).getResult()
        }

        factory(VERTX_SERVICE_DISCOVERY.qualifier) {
            val vertx: Vertx = get(VERTX_INSTANCE.qualifier)
            val serviceDiscoveryOptions: ServiceDiscoveryOptions = get(VERTX_SERVICE_DISCOVERY_OPTIONS.qualifier)

            getServiceDiscovery(vertx, serviceDiscoveryOptions)
        }
    }.toModuleWithOrder(moduleName = "vertxServiceDiscoveryOrderedKoinModule")

    override val koinOrderedModules: LinkedHashSet<ModuleWithOrder> = linkedSetOf(vertxServiceDiscoveryOrderedKoinModule)
}

object VertxServiceDiscoveryCommonModule {
    fun getServiceDiscoveryOptions(configRetriever: ConfigRetriever): FnResult<ServiceDiscoveryOptions> {
        return getActiveProfileProperty<ServiceDiscoveryApplicationProperties, ServiceDiscoveryProfileProperties>(configRetriever)
            .map { it.serviceDiscoveryOptions }
            .onFailure { FnResult.FnSuccess(defaultServiceDiscoveryOptions()) }
    }

    fun getServiceDiscovery(vertx: Vertx, serviceDiscoveryOptions: ServiceDiscoveryOptions): ServiceDiscovery =
        ServiceDiscovery.create(vertx, serviceDiscoveryOptions)

    internal fun defaultServiceDiscoveryOptions(): ServiceDiscoveryOptions = ServiceDiscoveryOptions()
        .setAnnounceAddress(DEFAULT_SERVICE_DISCOVERY_ANNOUNCE_ADDRESS).setName(DEFAULT_SERVICE_DISCOVERY_NAME)

    private const val DEFAULT_SERVICE_DISCOVERY_ANNOUNCE_ADDRESS = "vertx.cc.discovery.announce"
    private const val DEFAULT_SERVICE_DISCOVERY_NAME = "vertx.cc.discovery.name"
}

enum class VertServiceDiscoveryKoinQualifiers(val qualifier: StringQualifier) {
    VERTX_SERVICE_DISCOVERY_OPTIONS("VERTX_SERVICE_DISCOVERY_OPTIONS".qualifier()),
    VERTX_SERVICE_DISCOVERY("VERTX_SERVICE_DISCOVERY".qualifier());
}

interface ServiceDiscoveryInterface {
    fun healthCheck(handler: Handler<AsyncResult<Boolean>>): Unit
}

fun defaultHealthCheck(handler: Handler<AsyncResult<Boolean>>) = handler.handle(succeededFuture(true))
