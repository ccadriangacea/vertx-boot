package de.codecentric.vertx.koin.web.handler

import de.codecentric.koin.core.KoinComponentWithOptIn
import de.codecentric.util.fnresult.FnResult
import de.codecentric.vertx.koin.core.logger.loggerWithTab
import de.codecentric.vertx.koin.web.fn.RoutingContextFnResult
import de.codecentric.vertx.koin.web.fn.getDecodedRequestPath
import de.codecentric.vertx.koin.web.fn.getResponseWithStatus
import de.codecentric.vertx.koin.web.fn.getTextPlainResponse
import de.codecentric.vertx.koin.web.fn.mapToRoutingContext
import de.codecentric.vertx.koin.web.fn.returnErrorResponse
import de.codecentric.vertx.koin.web.fn.returnOkJsonResponse
import de.codecentric.vertx.koin.web.fn.returnOkTextResponse
import io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR
import io.vertx.core.Future
import io.vertx.core.Future.future
import io.vertx.core.Handler
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Route
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.getKoin
import java.util.logging.Level.FINER
import java.util.logging.Level.SEVERE

abstract class AbstractRouteHandler : Handler<RoutingContext>, KoinComponentWithOptIn {
    abstract val basePath: String
    abstract val registrationPath: String

    final override fun handle(routingContext: RoutingContext) {
        GlobalScope.launch {
            getKoin().logger.loggerWithTab(2, FINER) { " -> RouteHandler must handle [${routingContext.request().method()}] request on path: ${routingContext.getDecodedRequestPath()}" }

            try {
                handleAsync(routingContext).mapToRoutingContext(routingContext)
            } catch (@Suppress("TooGenericExceptionCaught") th: Throwable) {
                routingContext.returnErrorResponse(th, INTERNAL_SERVER_ERROR.code())
            }
        }
    }

    abstract suspend fun handleAsync(routingContext: RoutingContext): RoutingContextFnResult<*>
}

fun AbstractRouteHandler.setAsRouteHandler(router: Router): Route {
    getKoin().logger.loggerWithTab(2) { " <- Supporting REST calls for path: '$registrationPath'" }
    return router.route(registrationPath).handler(this)
}

fun <T> FnResult<T>.mapToRoutingContext(routingContext: RoutingContext): Future<Void> =
    when (this) {
        is FnResult.FnSuccess -> {
            getKoin().logger.loggerWithTab(2) { " <- mapToRoutingContext FnSuccess: ${this.result}" }
            when (this.result) {
                is RoutingContextFnResult<*> -> (this.result as RoutingContextFnResult<*>).mapToRoutingContext(routingContext)
                is JsonObject -> routingContext.returnOkJsonResponse(this.result as JsonObject)
                is JsonArray -> routingContext.returnOkJsonResponse(this.result as JsonArray)
                is Future<*> -> future { promise -> GlobalScope.launch { (this@mapToRoutingContext.result as Future<*>).mapEmpty<Void>().onComplete(promise) } }
                else -> routingContext.returnOkTextResponse(this.result.toString())
            }
        }
        is FnResult.FnError -> {
            // TODO implement an error handler maybe?
            getKoin().logger.loggerWithTab(2, SEVERE) { " <- mapToRoutingContext FnError: ${this.errorMessage} -> ${this.cause}" }
            routingContext.getTextPlainResponse()
            routingContext.getResponseWithStatus(this.statusCode)
            routingContext.end(this.errorMessage)
        }
        is FnResult.FnAsyncSuccessCancelled -> TODO("this is not supported in routingContext")
    }