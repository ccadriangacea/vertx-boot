package de.codecentric.vertx.koin.web.module

import de.codecentric.util.fnresult.FnResult
import de.codecentric.util.fnresult.handleThrowableAsync
import de.codecentric.util.fnresult.map
import de.codecentric.util.fnresult.onFailure
import de.codecentric.vertx.koin.core.module.VertxConfigCommonModule.getActiveProfileProperty
import de.codecentric.vertx.koin.web.fn.RoutingContextFnResult
import de.codecentric.vertx.koin.web.fn.RoutingContextFnResultType
import de.codecentric.vertx.koin.web.handler.SingleRouteHandler
import de.codecentric.vertx.koin.web.handler.setAsRouteHandler
import de.codecentric.vertx.koin.web.properties.HttpServerApplicationProperties
import de.codecentric.vertx.koin.web.properties.HttpServerProfileProperties
import io.vertx.config.ConfigRetriever
import io.vertx.core.Vertx
import io.vertx.core.http.HttpServer
import io.vertx.core.http.HttpServerOptions
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.handler.CorsHandler
import io.vertx.kotlin.coroutines.await

object VertxWebCommonModule {
    fun getDefaultPort(configRetriever: ConfigRetriever): FnResult<Int> =
        getActiveProfileProperty<HttpServerApplicationProperties, HttpServerProfileProperties>(configRetriever)
            .map { it.defaultPort }
            .onFailure { FnResult.FnSuccess(DEFAULT_HTTP_SERVER_PORT) }

    fun getHttpServerOptions(configRetriever: ConfigRetriever, port: Int): FnResult<HttpServerOptions> {
        val defaultHttpServerOptions = HttpServerOptions().apply { this.port = port }
        return getActiveProfileProperty<HttpServerApplicationProperties, HttpServerProfileProperties>(configRetriever)
            .map { it.httpServerOptions[port.toString()] ?: defaultHttpServerOptions }
            .onFailure { FnResult.FnSuccess(defaultHttpServerOptions) }
    }

    fun getSecurityOptions(configRetriever: ConfigRetriever): FnResult<Map<String, String>> =
        getActiveProfileProperty<HttpServerApplicationProperties, HttpServerProfileProperties>(configRetriever)
            .map { it.securityOptions }
            .onFailure { FnResult.FnSuccess(emptyMap()) }

    fun getRouter(vertx: Vertx, bodyHandler: BodyHandler, corsHandler: CorsHandler): Router =
        Router
            .router(vertx)
            .apply {
                // Add a body handler
                route().handler(bodyHandler)
                route().handler(corsHandler)

                PingSingleRouteHandler("/ping").setAsRouteHandler(this)

                // Add an error handler
                errorHandler(DEFAULT_HTTP_ERROR_CODE) {
                    System.err.println("errorHandler in router: ${it.failure().message}")

                    it.fail(it.failure())
                }
            }

    suspend fun getHttpServerAsync(httpServerOptions: HttpServerOptions, router: Router, vertx: Vertx): FnResult<HttpServer> =
        handleThrowableAsync { vertx.createHttpServer(httpServerOptions).requestHandler(router).listen().await() }

    private const val DEFAULT_HTTP_ERROR_CODE: Int = 500
    private const val DEFAULT_HTTP_SERVER_PORT = 8080
}

class PingSingleRouteHandler(basePath: String) : SingleRouteHandler(
    basePath,
    getHandlerAsync = { RoutingContextFnResult.HttpOk("pong", RoutingContextFnResultType.TEXT_PLAIN) }
)