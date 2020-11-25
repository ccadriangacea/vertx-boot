package de.codecentric.vertx.koin.gcp.core.extension

import com.google.api.gax.rpc.ApiException
import com.google.api.gax.rpc.StatusCode

object GcpCoreExtensions {
    fun <T> getOrCreate(getFunction: () -> T, createFunction: () -> T): T {
        return try {
            getFunction()
        } catch (ex: ApiException) {
            when (ex.statusCode.code.httpStatusCode) {
                StatusCode.Code.NOT_FOUND.httpStatusCode -> createFunction()
                else -> throw ex
            }
        }
    }
}