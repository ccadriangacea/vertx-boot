package de.codecentric.vertx.koin.web.fn

import de.codecentric.vertx.common.fn.FnResult
import de.codecentric.vertx.koin.web.exception.InternalServerException
import de.codecentric.vertx.koin.web.fn.RoutingContextFnResultType.APPLICATION_JSON
import de.codecentric.vertx.koin.web.fn.RoutingContextFnResultType.TEXT_PLAIN
import io.vertx.core.Future
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext

sealed class RoutingContextFnResult<T> {
    abstract val contentType: RoutingContextFnResultType

    data class HttpOk<T>(val result: T, override val contentType: RoutingContextFnResultType) : RoutingContextFnResult<T>()

    data class HttpNotFound<T>(val cause: Throwable) : RoutingContextFnResult<T>() {
        override val contentType: RoutingContextFnResultType = TEXT_PLAIN
    }

    data class HttpInternalServerError<T>(val cause: Throwable) : RoutingContextFnResult<T>() {
        override val contentType: RoutingContextFnResultType = TEXT_PLAIN
    }

    companion object {
        const val DEFAULT_RESPONSE_HTTP_STATUS_CODE_KEY = "httpStatusCode"
        const val DEFAULT_RESPONSE_HEADER_KEY = "headers"
        const val DEFAULT_RESPONSE_BODY_KEY = "body"
    }
}

fun <T> FnResult<T>.mapToRoutingContextFnResult() = when (this) {
    is FnResult.FnSuccess -> RoutingContextFnResult.HttpOk(this.result, TEXT_PLAIN)
    is FnResult.FnError -> RoutingContextFnResult.HttpInternalServerError(
        this.cause ?: InternalServerException("unknown exception with message: ${this.errorMessage}")
    )
    is FnResult.FnAsyncSuccessCancelled -> TODO()
}

fun <T> RoutingContextFnResult<T>.mapToRoutingContext(routingContext: RoutingContext): Future<Void> {
    return when (this) {
        is RoutingContextFnResult.HttpOk -> when (this@mapToRoutingContext.contentType) {
            TEXT_PLAIN -> routingContext.returnOkTextResponse(result.toString())
            APPLICATION_JSON -> routingContext.returnOkJsonResponse(JsonObject.mapFrom(result))
            else -> routingContext.returnOkTextResponse("Not implemented yet!")
        }
        is RoutingContextFnResult.HttpNotFound -> routingContext.returnErrorResponse(cause, 404)
        is RoutingContextFnResult.HttpInternalServerError -> routingContext.returnErrorResponse(cause)
    }
}

enum class RoutingContextFnResultType {
    TEXT_PLAIN,
    TEXT_HTML,
    APPLICATION_JSON
}
