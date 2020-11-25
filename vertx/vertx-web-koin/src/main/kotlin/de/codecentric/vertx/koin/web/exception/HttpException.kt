package de.codecentric.vertx.koin.web.exception

import de.codecentric.vertx.koin.web.fn.RoutingContextFnResult

abstract class HttpException(message: String) : Throwable(message) {
    abstract val httpStatusCode: Int
}

class NotFoundException(message: String, override val httpStatusCode: Int = 404) : HttpException(message)
class UnauthorizedException(message: String, override val httpStatusCode: Int = 401) : HttpException(message)
class ForbiddenException(message: String, override val httpStatusCode: Int = 403) : HttpException(message)
class InternalServerException(message: String, override val httpStatusCode: Int = 500) : HttpException(message)

fun <T> HttpException.toRoutingContextFnResult(): RoutingContextFnResult<Void> = when (this) {
    is NotFoundException -> RoutingContextFnResult.HttpNotFound(cause = this)
    // TODO add other exceptions to match a RoutingContextFnResult
    else -> RoutingContextFnResult.HttpInternalServerError(cause = this)
}
