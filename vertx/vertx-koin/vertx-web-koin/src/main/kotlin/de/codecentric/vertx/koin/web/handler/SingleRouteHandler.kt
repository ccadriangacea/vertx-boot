package de.codecentric.vertx.koin.web.handler

import de.codecentric.vertx.koin.web.exception.NotFoundException
import de.codecentric.vertx.koin.web.exception.toRoutingContextFnResult
import de.codecentric.vertx.koin.web.fn.RoutingContextFnResult
import de.codecentric.vertx.koin.web.fn.getDecodedRequestPath
import de.codecentric.vertx.koin.web.fn.matchPathToRegex
import io.vertx.core.http.HttpMethod.DELETE
import io.vertx.core.http.HttpMethod.GET
import io.vertx.core.http.HttpMethod.POST
import io.vertx.core.http.HttpMethod.PUT
import io.vertx.ext.web.RoutingContext

open class SingleRouteHandler(
    override val basePath: String,
    var getHandlerAsync: (suspend (RoutingContext) -> RoutingContextFnResult<*>)? = null,
    var postHandlerAsync: (suspend (RoutingContext) -> RoutingContextFnResult<*>)? = null,
    var putHandlerAsync: (suspend (RoutingContext) -> RoutingContextFnResult<*>)? = null,
    var deleteHandlerAsync: (suspend (RoutingContext) -> RoutingContextFnResult<*>)? = null
) : AbstractRouteHandler() {
    override val registrationPath: String
        get() = basePath

    @Suppress("ComplexMethod")
    override suspend fun handleAsync(routingContext: RoutingContext): RoutingContextFnResult<*> {
        if (!routingContext.getDecodedRequestPath().matchPathToRegex(basePath)) {
            return NotFoundException("This path[$this] was not found!").toRoutingContextFnResult<Unit>()
        }

        return when (routingContext.request().method()) {
            GET -> getHandlerAsync?.let { it(routingContext) } ?: NotFoundException("GET Not found...").toRoutingContextFnResult<Unit>()
            POST -> postHandlerAsync?.let { it(routingContext) } ?: NotFoundException("POST Not found...").toRoutingContextFnResult<Unit>()
            PUT -> putHandlerAsync?.let { it(routingContext) } ?: NotFoundException("PUT Not found...").toRoutingContextFnResult<Unit>()
            DELETE -> deleteHandlerAsync?.let { it(routingContext) } ?: NotFoundException("DELETE Not found...").toRoutingContextFnResult<Unit>()
            else -> NotFoundException("Not found...").toRoutingContextFnResult<Unit>()
        }
    }
}
