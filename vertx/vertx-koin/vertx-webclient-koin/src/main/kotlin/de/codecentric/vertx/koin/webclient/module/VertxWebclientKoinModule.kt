package de.codecentric.vertx.koin.webclient.module

import de.codecentric.koin.core.KoinModule
import de.codecentric.koin.core.KoinModuleWithOrder
import de.codecentric.koin.core.qualifier
import de.codecentric.koin.core.toKoinModuleWithOrder
import de.codecentric.util.fnresult.FnResult
import de.codecentric.util.fnresult.getResult
import de.codecentric.util.fnresult.map
import de.codecentric.util.fnresult.onFailure
import de.codecentric.vertx.koin.core.module.VertxConfigCommonModule.getActiveProfileProperty
import de.codecentric.vertx.koin.core.module.VertxConfigKoinQualifiers.VERTX_CONFIG_RETRIEVER
import de.codecentric.vertx.koin.core.module.VertxKoinQualifiers.VERTX_INSTANCE
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
    }.toKoinModuleWithOrder(moduleName = "webclientOrderedKoinModule")

    override val koinOrderedModules: LinkedHashSet<KoinModuleWithOrder> = linkedSetOf(webclientOrderedKoinModule)
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
