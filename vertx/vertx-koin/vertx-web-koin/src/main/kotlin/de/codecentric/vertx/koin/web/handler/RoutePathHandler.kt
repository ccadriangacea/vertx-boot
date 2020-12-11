@file:Suppress("FunctionName")

package de.codecentric.vertx.koin.web.handler

import de.codecentric.koin.core.KoinComponentWithOptIn
import de.codecentric.vertx.koin.core.logger.loggerWithTab
import de.codecentric.vertx.koin.web.exception.NotFoundException
import de.codecentric.vertx.koin.web.exception.toRoutingContextFnResult
import de.codecentric.vertx.koin.web.fn.RoutingContextFnResult
import de.codecentric.vertx.koin.web.fn.getDecodedRequestPath
import io.vertx.core.http.HttpMethod
import io.vertx.core.http.HttpMethod.DELETE
import io.vertx.core.http.HttpMethod.GET
import io.vertx.core.http.HttpMethod.POST
import io.vertx.core.http.HttpMethod.PUT
import io.vertx.ext.web.RoutingContext
import java.util.logging.Level.FINER

annotation class RestApiDescription(val description: String)

annotation class RestApiBasePath(val value: String)

interface RoutePathHandler {
    val routingContext: RoutingContext

    suspend fun GET(): RoutingContextFnResult<*> = NotFoundException("GET Not found...").toRoutingContextFnResult<Unit>()

    suspend fun POST(): RoutingContextFnResult<*> = NotFoundException("GET Not found...").toRoutingContextFnResult<Unit>()

    suspend fun PUT(): RoutingContextFnResult<*> = NotFoundException("GET Not found...").toRoutingContextFnResult<Unit>()

    suspend fun DELETE(): RoutingContextFnResult<*> = NotFoundException("GET Not found...").toRoutingContextFnResult<Unit>()

    suspend fun handle(): RoutingContextFnResult<*>
}

abstract class AbstractRoutePathHandler : RoutePathHandler, KoinComponentWithOptIn {
    final override suspend fun handle(): RoutingContextFnResult<*> {
        val (_, requestMethod) = logRequest()

        return when (requestMethod) {
            GET -> GET()
            POST -> POST()
            PUT -> PUT()
            DELETE -> DELETE()
            else -> NotFoundException("RequestMethod $requestMethod not found").toRoutingContextFnResult<Unit>()
        }
    }

    private fun logRequest(): Pair<String, HttpMethod> {
        val requestPath = routingContext.getDecodedRequestPath()
        val requestMethod = routingContext.request().method()

        getKoin().logger.apply {
            loggerWithTab(1, FINER) { " -> $requestMethod on path: $requestPath with params: ${routingContext.queryParams()}" }
            loggerWithTab(1, FINER) { " -> $requestMethod on path: $requestPath with headers: ${routingContext.request().headers()}" }
        }

        return Pair(requestPath, requestMethod)
    }
}
