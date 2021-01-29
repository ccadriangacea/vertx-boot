package de.codecentric.util.fnresult

import de.codecentric.util.fnresult.exception.FnResultFutureException

sealed class FnResult<T> {
    data class FnSuccess<T>(val result: T) : FnResult<T>()
    data class FnAsyncSuccessCancelled<T>(val result: T) : FnResult<T>()

    data class FnError<T>(val errorMessage: String, val cause: Throwable? = null, val statusCode: Int = DEFAULT_HTTP_STATUS_CODE) : FnResult<T>() {
        constructor(cause: Throwable?) : this(cause?.localizedMessage ?: "Unknown cause", cause)

        constructor(errorMessage: String, statusCode: Int = DEFAULT_HTTP_STATUS_CODE) : this(errorMessage, null, statusCode)
    }

    companion object {
        private const val DEFAULT_HTTP_STATUS_CODE: Int = 500

        fun fnErrorEmpty(): FnError<Unit> = FnError(FnResultFutureException("Empty error"))
    }
}

fun <T> handleThrowable(function: () -> T): FnResult<T> =
    try {
        FnResult.FnSuccess(function())
    } catch (th: Throwable) {
        System.err.println("handleThrowable exception: $th")
        FnResult.FnError(th)
    }

fun <T> handleThrowableOfFnResult(function: () -> FnResult<T>): FnResult<T> =
    try {
        function()
    } catch (th: Throwable) {
        System.err.println("handleThrowableOfFnResult exception: $th")
        FnResult.FnError(th)
    }

suspend fun <T> handleThrowableAsync(function: suspend () -> T): FnResult<T> =
    try {
        FnResult.FnSuccess(function())
    } catch (th: Throwable) {
        System.err.println("handleThrowableAsync exception: $th")
        FnResult.FnError(errorMessage = "something went wrong", cause = th)
    }

suspend fun <T> handleThrowableOfFnResultAsync(function: suspend () -> FnResult<T>): FnResult<T> =
    try {
        function()
    } catch (th: Throwable) {
        System.err.println("handleThrowableOfFnResultAsync exception: $th")
        FnResult.FnError(th)
    }
