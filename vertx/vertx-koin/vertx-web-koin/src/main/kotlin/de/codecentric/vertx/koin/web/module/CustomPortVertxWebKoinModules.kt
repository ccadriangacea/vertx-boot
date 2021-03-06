package de.codecentric.vertx.koin.web.module

import de.codecentric.koin.core.KoinModule
import de.codecentric.koin.core.KoinModuleWithOrder
import de.codecentric.koin.core.qualifier
import de.codecentric.koin.core.toKoinModuleWithOrder
import de.codecentric.util.fnresult.getResult
import de.codecentric.vertx.koin.core.module.VertxConfigKoinQualifiers.VERTX_CONFIG_RETRIEVER
import de.codecentric.vertx.koin.core.module.VertxKoinQualifiers.VERTX_INSTANCE
import de.codecentric.vertx.koin.web.module.CustomPortVertxWebKoinQualifiers.VERTX_HTTPSERVER_CUSTOM_PORT
import de.codecentric.vertx.koin.web.module.CustomPortVertxWebKoinQualifiers.VERTX_HTTPSERVER_CUSTOM_PORT_SCOPE
import de.codecentric.vertx.koin.web.module.CustomPortVertxWebKoinQualifiers.VERTX_HTTPSERVER_OPTIONS_CUSTOM_PORT
import de.codecentric.vertx.koin.web.module.CustomPortVertxWebKoinQualifiers.VERTX_HTTPSERVER_ROUTER_CUSTOM_PORT
import de.codecentric.vertx.koin.web.module.VertxWebCommonModule.getHttpServerAsync
import de.codecentric.vertx.koin.web.module.VertxWebCommonModule.getHttpServerOptions
import de.codecentric.vertx.koin.web.module.VertxWebCommonModule.getRouter
import de.codecentric.vertx.koin.web.module.VertxWebHandlersKoinQualifiers.VERTX_HTTPSERVER_BODY_HANDLER
import de.codecentric.vertx.koin.web.module.VertxWebHandlersKoinQualifiers.VERTX_HTTPSERVER_CORS_HANDLER
import io.vertx.config.ConfigRetriever
import io.vertx.core.Vertx
import io.vertx.core.http.HttpServerOptions
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.handler.CorsHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.StringQualifier
import org.koin.dsl.module
import java.util.LinkedHashSet

open class CustomPortVertxWebKoinModules : KoinModule {
    private val customPortVertxWebOrderedKoinModule = module {
        scope(VERTX_HTTPSERVER_CUSTOM_PORT_SCOPE.qualifier) {
            scoped(VERTX_HTTPSERVER_OPTIONS_CUSTOM_PORT.qualifier) { (port: Int) ->
                val configRetriever: ConfigRetriever = get(VERTX_CONFIG_RETRIEVER.qualifier)

                getHttpServerOptions(configRetriever, port).getResult()
            }

            scoped(VERTX_HTTPSERVER_ROUTER_CUSTOM_PORT.qualifier) { (port: Int) ->
                val vertx: Vertx = get(VERTX_INSTANCE.qualifier)
                val bodyHandler: BodyHandler = get(VERTX_HTTPSERVER_BODY_HANDLER.qualifier)
                val corsHandler: CorsHandler = get(VERTX_HTTPSERVER_CORS_HANDLER.qualifier) { parametersOf("http://localhost:$port") }

                getRouter(vertx, bodyHandler, corsHandler)
            }

            scoped(VERTX_HTTPSERVER_CUSTOM_PORT.qualifier) { (port: Int) ->
                val httpServerOptions: HttpServerOptions = get(VERTX_HTTPSERVER_OPTIONS_CUSTOM_PORT.qualifier) { parametersOf(port) }
                val router: Router = get(VERTX_HTTPSERVER_ROUTER_CUSTOM_PORT.qualifier) { parametersOf(port) }
                val vertx: Vertx = get(VERTX_INSTANCE.qualifier)

                runBlocking {
                    withContext(Dispatchers.Default) { getHttpServerAsync(httpServerOptions, router, vertx).getResult() }
                }
            }
        }
    }.toKoinModuleWithOrder(moduleName = "customPortVertxWebOrderedKoinModule")

    override val koinOrderedModules: LinkedHashSet<KoinModuleWithOrder> = linkedSetOf(customPortVertxWebOrderedKoinModule)
}

enum class CustomPortVertxWebKoinQualifiers(val qualifier: StringQualifier) {
    VERTX_HTTPSERVER_CUSTOM_PORT_SCOPE("VERTX_HTTPSERVER_CUSTOM_PORT_SCOPE".qualifier()),
    VERTX_HTTPSERVER_OPTIONS_CUSTOM_PORT("VERTX_HTTPSERVER_OPTIONS_CUSTOM_PORT".qualifier()),
    VERTX_HTTPSERVER_ROUTER_CUSTOM_PORT("VERTX_HTTPSERVER_ROUTER_CUSTOM_PORT".qualifier()),
    VERTX_HTTPSERVER_CUSTOM_PORT("VERTX_HTTPSERVER_CUSTOM_PORT".qualifier()),
}
