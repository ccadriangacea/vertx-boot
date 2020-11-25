package de.codecentric.vertx.koin.web.fn

import de.codecentric.vertx.koin.web.exception.HttpException
import de.codecentric.vertx.koin.web.fn.RoutingContextFnResult.Companion.DEFAULT_RESPONSE_BODY_KEY
import de.codecentric.vertx.koin.web.fn.RoutingContextFnResult.Companion.DEFAULT_RESPONSE_HEADER_KEY
import de.codecentric.vertx.koin.web.fn.RoutingContextFnResult.Companion.DEFAULT_RESPONSE_HTTP_STATUS_CODE_KEY
import de.codecentric.vertx.common.fn.FnResult
import io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST
import io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR
import io.vertx.core.Future
import io.vertx.core.http.HttpServerResponse
import io.vertx.core.json.DecodeException
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import java.net.URLDecoder

fun RoutingContext.getTextPlainResponse(): HttpServerResponse =
    this.response().putHeader("content-type", "text/plain; charset=UTF-8")

fun RoutingContext.getTextHtmlResponse(): HttpServerResponse =
    this.response().putHeader("content-type", "text/html; charset=UTF-8")

fun RoutingContext.getJsonResponse(): HttpServerResponse =
    this.response().putHeader("content-type", "application/json")

fun RoutingContext.getResponseWithStatus(statusCode: Int): HttpServerResponse =
    this.response().setStatusCode(statusCode)

fun RoutingContext.returnOkTextResponse(message: String, successStatusCode: Int = 200): Future<Void> {
    this.apply {
        getTextPlainResponse()
        getResponseWithStatus(successStatusCode)
    }
    return this.end(message)
}

fun RoutingContext.returnOkJsonResponse(jsonObject: JsonObject, successStatusCode: Int = 200): Future<Void> {
    this.getJsonResponse()

    // HEADERS
    val headers: JsonObject = jsonObject.getJsonObject(DEFAULT_RESPONSE_HEADER_KEY, JsonObject())
    headers.map.forEach { headerPair -> this.response().putHeader(headerPair.key, headerPair.value.toString()) }
    jsonObject.remove(DEFAULT_RESPONSE_HEADER_KEY)

    // HTTP STATUS
    val httpStatusCode = jsonObject.getInteger(DEFAULT_RESPONSE_HTTP_STATUS_CODE_KEY, successStatusCode)
    jsonObject.remove(DEFAULT_RESPONSE_HTTP_STATUS_CODE_KEY)
    getResponseWithStatus(httpStatusCode)

    // BODY
    val body = jsonObject.getJsonObject(DEFAULT_RESPONSE_BODY_KEY) ?: JsonObject()
    return end(body.toBuffer())
}

fun RoutingContext.returnOkJsonResponse(jsonArray: JsonArray, successStatusCode: Int = 200): Future<Void> {
    this.apply {
        getJsonResponse()
        getResponseWithStatus(successStatusCode)
    }

    return end(jsonArray.toBuffer())
}

fun RoutingContext.returnErrorResponse(th: Throwable, errorStatusCode: Int = INTERNAL_SERVER_ERROR.code()): Future<Void> {
    this.getTextPlainResponse()

    return when (th) {
        is IllegalArgumentException -> this.getResponseWithStatus(BAD_REQUEST.code()).end("Illegal body!")
        is DecodeException -> this.getResponseWithStatus(BAD_REQUEST.code()).end("Decode exception!")
        is HttpException -> this.getResponseWithStatus(th.httpStatusCode).end(th.message)
        else -> this.getResponseWithStatus(errorStatusCode).end(th.localizedMessage)
    }
}

fun RoutingContext.getDecodedRequestPath(): String = URLDecoder.decode(this.request().path(), "UTF-8")

fun String.matchPathToRegex(pathString: String) = this.matches(Regex(pathString))

fun String.extractVariables(regex: Regex) = regex.find(this)!!.destructured

inline fun <reified T> RoutingContext.bodyAsObject(): FnResult<T> =
    try {
        val t = this.bodyAsJson.mapTo(T::class.java)
        FnResult.FnSuccess(t)
    } catch (@Suppress("TooGenericExceptionCaught") th: Throwable) {
        FnResult.FnError(th)
    }
