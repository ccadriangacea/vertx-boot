package de.codecentric.vertx.koin.webclient.module

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
import de.codecentric.vertx.koin.webclient.module.VertxWebclientCommonModule.getWebClient
import de.codecentric.vertx.koin.webclient.module.VertxWebclientCommonModule.getWebClientOptions
import de.codecentric.vertx.koin.webclient.module.VertxWebclientKoinQualifiers.VERTX_WEB_CLIENT
import de.codecentric.vertx.koin.webclient.module.VertxWebclientKoinQualifiers.VERTX_WEB_CLIENT_OPTION
import de.codecentric.vertx.koin.webclient.properties.WebClientApplicationProperties
import de.codecentric.vertx.koin.webclient.properties.WebClientProfileProperties
import io.vertx.config.ConfigRetriever
import io.vertx.core.Vertx
import io.vertx.ext.web.client.WebClient
import io.vertx.ext.web.client.WebClientOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.koin.core.qualifier.StringQualifier
import org.koin.dsl.module

class VertxWebclientKoinModule : KoinModule {
    private val webclientOrderedKoinModule = module {
        single(VERTX_WEB_CLIENT_OPTION.qualifier) {
            val configRetriever: ConfigRetriever = get(VERTX_CONFIG_RETRIEVER.qualifier)

            runBlocking {
                withContext(Dispatchers.Default) { getWebClientOptions(configRetriever).getResult() }
            }
        }

        factory(VERTX_WEB_CLIENT.qualifier) {
            val vertx: Vertx = get(VERTX_INSTANCE.qualifier)
            val webClientOptions: WebClientOptions = get(VERTX_WEB_CLIENT_OPTION.qualifier)

            getWebClient(vertx, webClientOptions)
        }
    }.toModuleWithOrder(moduleName = "webclientOrderedKoinModule")

    override val koinOrderedModules: LinkedHashSet<ModuleWithOrder> = linkedSetOf(webclientOrderedKoinModule)
}

object VertxWebclientCommonModule {
    fun getWebClientOptions(configRetriever: ConfigRetriever): FnResult<WebClientOptions> =
        getActiveProfileProperty<WebClientApplicationProperties, WebClientProfileProperties>(configRetriever)
            .map { it.webClientOptions }
            .onFailure { FnResult.FnSuccess(WebClientOptions()) }

    fun getWebClient(vertx: Vertx, webClientOptions: WebClientOptions): WebClient = WebClient.create(vertx, webClientOptions)
}

enum class VertxWebclientKoinQualifiers(val qualifier: StringQualifier) {
    VERTX_WEB_CLIENT_OPTION("VERTX_WEB_CLIENT_OPTION".qualifier()),
    VERTX_WEB_CLIENT("VERTX_WEB_CLIENT".qualifier());
}
